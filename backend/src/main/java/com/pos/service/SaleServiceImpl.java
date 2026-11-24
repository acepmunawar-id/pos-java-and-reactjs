package com.pos.service;

import com.pos.exception.BadRequestException;
import com.pos.exception.InsufficientStockException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.model.Category;
import com.pos.model.Customer;
import com.pos.repository.CategoryRepository;
import com.pos.repository.CustomerRepository;
import com.pos.model.Discount;
import com.pos.model.DiscountType;
import com.pos.model.Tax;
import com.pos.repository.DiscountRepository;
import com.pos.repository.TaxRepository;
import com.pos.model.Product;
import com.pos.repository.ProductRepository;
import com.pos.dto.SaleItemRequest;
import com.pos.dto.SaleRequest;
import com.pos.dto.SaleResponse;
import com.pos.model.Sale;
import com.pos.model.SaleItem;
import com.pos.model.PaymentStatus;
import com.pos.model.SaleStatus;
import com.pos.mapper.SaleMapper;
import com.pos.repository.SaleRepository;
import com.pos.security.Role;
import com.pos.security.SecurityUtils;
import com.pos.model.User;
import com.pos.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final TaxRepository taxRepository;
    private final DiscountRepository discountRepository;
    private final UserRepository userRepository;
    private final SaleMapper saleMapper;

    @Override
    public SaleResponse create(SaleRequest request) {
        User cashier = currentUser();

        Sale sale = Sale.builder()
                .invoiceNumber(generateInvoiceNumber())
                .saleDate(Instant.now())
                .cashier(cashier)
                .status(SaleStatus.COMPLETED)
                .build();

        if (request.getCustomerId() != null) {
            sale.setCustomer(resolveCustomer(request.getCustomerId()));
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaleItemRequest itemRequest : request.getItems()) {
            // Pessimistic lock avoids two concurrent checkouts overselling the same product.
            Product product = productRepository
                    .findByIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Produk", itemRequest.getProductId()));
            // Kunci kategori SEBELUM datanya dibaca, supaya stok bahan campuran (milik
            // kategori, dipakai bersama banyak produk) selalu nilai terbaru.
            if (product.getCategory() != null) {
                lockCategory(product.getCategory().getId());
            }

            SaleItem item = product.isMeasured()
                    ? sellMeasured(product, itemRequest)
                    : sellGeneral(product, itemRequest);
            productRepository.save(product);
            subtotal = subtotal.add(item.getSubtotal());
            sale.addItem(item);
        }
        subtotal = round(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscountId() != null) {
            Discount discount = resolveDiscount(request.getDiscountId());
            sale.setDiscount(discount);
            discountAmount = discount.getType() == DiscountType.PERCENTAGE
                    ? subtotal.multiply(discount.getValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : discount.getValue();
            if (discountAmount.compareTo(subtotal) > 0) {
                discountAmount = subtotal; // never discount below zero
            }
        }

        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (request.getTaxId() != null) {
            Tax tax = resolveTax(request.getTaxId());
            sale.setTax(tax);
            taxAmount = taxableAmount.multiply(tax.getRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal total = taxableAmount.add(taxAmount);
        boolean isDebt = request.getPaymentStatus() == PaymentStatus.UNPAID;

        if (isDebt) {
            if (sale.getCustomer() == null) {
                throw new BadRequestException("Transaksi hutang wajib memilih pelanggan");
            }
        } else {
            BigDecimal paid = request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO;
            if (paid.compareTo(total) < 0) {
                throw new BadRequestException("Jumlah bayar (" + paid + ") kurang dari total tagihan (" + total + ")");
            }
        }

        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(round(discountAmount));
        sale.setTaxAmount(round(taxAmount));
        sale.setTotal(round(total));
        sale.setPaymentStatus(isDebt ? PaymentStatus.UNPAID : PaymentStatus.PAID);
        sale.setPaymentMethod(isDebt ? null : request.getPaymentMethod());
        BigDecimal paid = isDebt ? BigDecimal.ZERO : (request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO);
        sale.setAmountPaid(paid);
        sale.setChangeAmount(isDebt ? BigDecimal.ZERO : round(paid.subtract(total)));

        return saleMapper.toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleResponse> findAll(LocalDate from, LocalDate to, Pageable pageable) {
        Instant fromInstant = (from == null ? LocalDate.now().minusMonths(1) : from)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = (to == null ? LocalDate.now() : to)
                .plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        Page<Sale> sales = SecurityUtils.currentUserHasRole(Role.USER)
                ? saleRepository.findByCashierIdAndSaleDateBetween(currentUser().getId(), fromInstant, toInstant, pageable)
                : saleRepository.findBySaleDateBetween(fromInstant, toInstant, pageable);

        return sales.map(saleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse findById(UUID id) {
        Sale sale = getOrThrow(id);
        enforceOwnershipForCashier(sale);
        return saleMapper.toResponse(sale);
    }

    @Override
    public SaleResponse voidSale(UUID id) {
        Sale sale = getOrThrow(id);
        if (sale.getStatus() == SaleStatus.VOID) {
            throw new BadRequestException("Transaksi ini sudah dibatalkan sebelumnya");
        }
        sale.getItems().forEach(item -> {
            Product product = productRepository
                    .findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Produk", item.getProduct().getId()));
            if (item.getSizeAmount() != null) {
                // Produk per takaran: kembalikan bahan utama & campuran yang terpakai.
                product.setMaterialStock(product.getMaterialStock().add(nz(item.getMaterialUsed())));
                if (nz(item.getMixerUsed()).signum() > 0 && product.getCategory() != null) {
                    Category category = lockCategory(product.getCategory().getId());
                    category.setMixerStock(category.getMixerStock().add(item.getMixerUsed()));
                }
            } else {
                product.setStock(product.getStock() + item.getQuantity());
            }
            productRepository.save(product);
        });
        sale.setStatus(SaleStatus.VOID);
        return saleMapper.toResponse(saleRepository.save(sale));
    }

    /** Produk umum: stok dalam pcs, harga jual tetap. */
    private SaleItem sellGeneral(Product product, SaleItemRequest request) {
        int qty = request.getQuantity();
        if (product.getStock() < qty) {
            throw new InsufficientStockException(product.getName(), product.getStock(), qty);
        }
        product.setStock(product.getStock() - qty);
        BigDecimal price = product.getSellPrice();
        return SaleItem.builder()
                .product(product)
                .productName(product.getName())
                .price(price)
                .quantity(qty)
                .subtotal(round(price.multiply(BigDecimal.valueOf(qty))))
                .unitCost(product.getPurchasePrice())
                .build();
    }

    /**
     * Produk per takaran: harga sesuai ukuran kemasan, stok bahan utama (produk) &
     * bahan campuran (kategori) dikurangi sesuai takaran x qty.
     */
    private SaleItem sellMeasured(Product product, SaleItemRequest request) {
        BigDecimal size = request.getSizeAmount();
        if (size == null) {
            throw new BadRequestException("Pilih ukuran untuk produk '" + product.getName() + "'");
        }
        Category category = lockCategory(product.getCategory().getId());
        String unit = category.getBaseUnit();
        String sizeLabel = size.stripTrailingZeros().toPlainString() + " " + unit;
        BigDecimal price = product.sellPriceFor(size)
                .orElseThrow(() -> new BadRequestException("Produk '" + product.getName() + "' tidak dijual ukuran " + sizeLabel));

        BigDecimal qty = BigDecimal.valueOf(request.getQuantity());
        BigDecimal materialUsed = category.materialAmountFor(size).multiply(qty);
        BigDecimal mixerUsed = category.mixerAmountFor(size).multiply(qty);

        if (product.getMaterialStock().compareTo(materialUsed) < 0) {
            throw new InsufficientStockException("Stok bahan '" + product.getName() + "' tidak mencukupi. Tersedia: "
                    + fmt(product.getMaterialStock()) + " " + unit + ", dibutuhkan: " + fmt(materialUsed) + " " + unit);
        }
        if (mixerUsed.signum() > 0 && category.getMixerStock().compareTo(mixerUsed) < 0) {
            String mixer = category.getMixerName() == null ? "campuran" : category.getMixerName();
            throw new InsufficientStockException("Stok " + mixer + " tidak mencukupi. Tersedia: "
                    + fmt(category.getMixerStock()) + " " + unit + ", dibutuhkan: " + fmt(mixerUsed) + " " + unit);
        }

        BigDecimal unitCost = product.unitCostFor(size);
        product.setMaterialStock(product.getMaterialStock().subtract(materialUsed));
        category.setMixerStock(category.getMixerStock().subtract(mixerUsed));

        return SaleItem.builder()
                .product(product)
                .productName(product.getName() + " " + sizeLabel)
                .price(price)
                .quantity(request.getQuantity())
                .subtotal(round(price.multiply(qty)))
                .sizeAmount(size)
                .materialUsed(materialUsed)
                .mixerUsed(mixerUsed)
                .unitCost(unitCost)
                .build();
    }

    private Category lockCategory(UUID id) {
        return categoryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Kategori", id));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String fmt(BigDecimal value) {
        return value.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private void enforceOwnershipForCashier(Sale sale) {
        if (SecurityUtils.currentUserHasRole(Role.USER)
                && !sale.getCashier().getUsername().equals(SecurityUtils.currentUsername())) {
            throw new AccessDeniedException("Anda hanya dapat melihat transaksi milik sendiri");
        }
    }

    private String generateInvoiceNumber() {
        long seq = saleRepository.nextInvoiceSequence();
        String datePart = INVOICE_DATE_FORMAT.format(LocalDate.now());
        return "INV-%s-%06d".formatted(datePart, seq);
    }

    private User currentUser() {
        String username = SecurityUtils.currentUsername();
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User '" + username + "' tidak ditemukan"));
    }

    private Sale getOrThrow(UUID id) {
        return saleRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Transaksi", id));
    }

    private Customer resolveCustomer(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Pelanggan", id));
    }

    private Tax resolveTax(UUID id) {
        return taxRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Pajak", id));
    }

    private Discount resolveDiscount(UUID id) {
        return discountRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Diskon", id));
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
