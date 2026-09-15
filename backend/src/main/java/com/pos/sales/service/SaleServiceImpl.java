package com.pos.sales.service;

import com.pos.common.exception.BadRequestException;
import com.pos.common.exception.InsufficientStockException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.partner.entity.Customer;
import com.pos.partner.repository.CustomerRepository;
import com.pos.pricing.entity.Discount;
import com.pos.pricing.entity.DiscountType;
import com.pos.pricing.entity.Tax;
import com.pos.pricing.repository.DiscountRepository;
import com.pos.pricing.repository.TaxRepository;
import com.pos.product.entity.Product;
import com.pos.product.repository.ProductRepository;
import com.pos.sales.dto.SaleItemRequest;
import com.pos.sales.dto.SaleRequest;
import com.pos.sales.dto.SaleResponse;
import com.pos.sales.entity.Sale;
import com.pos.sales.entity.SaleItem;
import com.pos.sales.entity.SaleStatus;
import com.pos.sales.mapper.SaleMapper;
import com.pos.sales.repository.SaleRepository;
import com.pos.security.Role;
import com.pos.security.SecurityUtils;
import com.pos.user.entity.User;
import com.pos.user.repository.UserRepository;
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

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(product.getName(), product.getStock(), itemRequest.getQuantity());
            }

            BigDecimal itemSubtotal = product.getSellPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);

            sale.addItem(SaleItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .price(product.getSellPrice())
                    .quantity(itemRequest.getQuantity())
                    .subtotal(round(itemSubtotal))
                    .build());
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
        if (request.getAmountPaid().compareTo(total) < 0) {
            throw new BadRequestException("Jumlah bayar (" + request.getAmountPaid() + ") kurang dari total tagihan (" + total + ")");
        }

        sale.setSubtotal(subtotal);
        sale.setDiscountAmount(round(discountAmount));
        sale.setTaxAmount(round(taxAmount));
        sale.setTotal(round(total));
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setAmountPaid(request.getAmountPaid());
        sale.setChangeAmount(round(request.getAmountPaid().subtract(total)));

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
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        });
        sale.setStatus(SaleStatus.VOID);
        return saleMapper.toResponse(saleRepository.save(sale));
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
