package com.pos.sales.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.BadRequestException;
import com.pos.common.exception.InsufficientStockException;
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
import com.pos.sales.entity.PaymentMethod;
import com.pos.sales.entity.Sale;
import com.pos.sales.mapper.SaleMapper;
import com.pos.sales.repository.SaleRepository;
import com.pos.security.Role;
import com.pos.user.entity.User;
import com.pos.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TaxRepository taxRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private UserRepository userRepository;

    private SaleServiceImpl saleService;

    private User cashier;
    private Product product;

    @BeforeEach
    void setUp() {
        SaleMapper saleMapper = Mappers.getMapper(SaleMapper.class);
        saleService = new SaleServiceImpl(
                saleRepository, productRepository, customerRepository, taxRepository, discountRepository,
                userRepository, saleMapper);

        cashier = User.builder()
                .id(UUID.randomUUID())
                .username("kasir")
                .fullName("Kasir Toko")
                .role(Role.USER)
                .active(true)
                .build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .sku("SKU-001")
                .name("Kopi Sachet")
                .purchasePrice(new BigDecimal("2000"))
                .sellPrice(new BigDecimal("3000"))
                .stock(50)
                .minStock(10)
                .active(true)
                .build();

        authenticateAs("kasir", Role.USER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username, Role role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private SaleRequest requestFor(int quantity, BigDecimal amountPaid) {
        SaleItemRequest item = new SaleItemRequest();
        item.setProductId(product.getId());
        item.setQuantity(quantity);

        SaleRequest request = new SaleRequest();
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setAmountPaid(amountPaid);
        request.setItems(List.of(item));
        return request;
    }

    @Test
    void create_computesTotalAndDecrementsStock_whenNoTaxOrDiscount() {
        when(userRepository.findByUsername("kasir")).thenReturn(Optional.of(cashier));
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(saleRepository.nextInvoiceSequence()).thenReturn(1L);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleResponse response = saleService.create(requestFor(2, new BigDecimal("6000")));

        assertThat(response.getSubtotal()).isEqualByComparingTo("6000");
        assertThat(response.getTotal()).isEqualByComparingTo("6000");
        assertThat(response.getChangeAmount()).isEqualByComparingTo("0");
        assertThat(product.getStock()).isEqualTo(48); // 50 - 2
        assertThat(response.getInvoiceNumber()).startsWith("INV-");
    }

    @Test
    void create_appliesPercentageDiscountThenTaxOnDiscountedAmount() {
        UUID discountId = UUID.randomUUID();
        UUID taxId = UUID.randomUUID();
        Discount discount = Discount.builder()
                .id(discountId).name("Diskon 10%").type(DiscountType.PERCENTAGE).value(new BigDecimal("10")).active(true)
                .build();
        Tax tax = Tax.builder().id(taxId).name("PPN 11%").rate(new BigDecimal("11")).active(true).build();

        when(userRepository.findByUsername("kasir")).thenReturn(Optional.of(cashier));
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(discount));
        when(taxRepository.findById(taxId)).thenReturn(Optional.of(tax));
        when(saleRepository.nextInvoiceSequence()).thenReturn(2L);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        SaleRequest request = requestFor(1, new BigDecimal("3000"));
        request.setDiscountId(discountId);
        request.setTaxId(taxId);

        SaleResponse response = saleService.create(request);

        // subtotal 3000, discount 10% = 300 -> taxable 2700, tax 11% = 297 -> total 2997
        assertThat(response.getSubtotal()).isEqualByComparingTo("3000");
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("300");
        assertThat(response.getTaxAmount()).isEqualByComparingTo("297.00");
        assertThat(response.getTotal()).isEqualByComparingTo("2997.00");
    }

    @Test
    void create_throwsInsufficientStock_whenRequestedQuantityExceedsAvailableStock() {
        when(userRepository.findByUsername("kasir")).thenReturn(Optional.of(cashier));
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> saleService.create(requestFor(999, new BigDecimal("999999"))))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void create_throwsBadRequest_whenAmountPaidLessThanTotal() {
        when(userRepository.findByUsername("kasir")).thenReturn(Optional.of(cashier));
        when(productRepository.findByIdForUpdate(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> saleService.create(requestFor(2, new BigDecimal("1000"))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void findById_throwsAccessDenied_whenCashierTriesToViewAnotherCashiersSale() {
        User otherCashier = User.builder()
                .id(UUID.randomUUID()).username("kasir-lain").fullName("Kasir Lain").role(Role.USER).active(true)
                .build();
        Sale sale = Sale.builder()
                .id(UUID.randomUUID())
                .invoiceNumber("INV-20260101-000001")
                .cashier(otherCashier)
                .subtotal(BigDecimal.TEN)
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .total(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.CASH)
                .amountPaid(BigDecimal.TEN)
                .changeAmount(BigDecimal.ZERO)
                .build();
        when(saleRepository.findById(sale.getId())).thenReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.findById(sale.getId())).isInstanceOf(AccessDeniedException.class);
    }
}
