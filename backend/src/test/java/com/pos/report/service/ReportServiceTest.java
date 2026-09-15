package com.pos.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.partner.entity.Customer;
import com.pos.product.entity.Product;
import com.pos.sales.entity.PaymentMethod;
import com.pos.sales.entity.Sale;
import com.pos.sales.entity.SaleItem;
import com.pos.sales.entity.SaleStatus;
import com.pos.sales.repository.SaleRepository;
import com.pos.product.repository.ProductRepository;
import com.pos.security.Role;
import com.pos.user.entity.User;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    private ReportService reportService;

    private Sale sale;
    private Product product;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(saleRepository, productRepository);

        User cashier = User.builder().id(UUID.randomUUID()).username("kasir").fullName("Kasir Toko").role(Role.USER).build();
        Customer customer = Customer.builder().id(UUID.randomUUID()).name("Budi Santoso").build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .sku("SKU-001")
                .name("Kopi Sachet")
                .purchasePrice(new BigDecimal("2000"))
                .sellPrice(new BigDecimal("3000"))
                .stock(48)
                .minStock(10)
                .active(true)
                .build();

        sale = Sale.builder()
                .id(UUID.randomUUID())
                .invoiceNumber("INV-20260101-000001")
                .saleDate(Instant.parse("2026-01-01T03:00:00Z"))
                .cashier(cashier)
                .customer(customer)
                .subtotal(new BigDecimal("6000"))
                .discountAmount(new BigDecimal("0"))
                .taxAmount(new BigDecimal("660"))
                .total(new BigDecimal("6660"))
                .paymentMethod(PaymentMethod.CASH)
                .amountPaid(new BigDecimal("10000"))
                .changeAmount(new BigDecimal("3340"))
                .status(SaleStatus.COMPLETED)
                .build();
        sale.addItem(SaleItem.builder()
                .product(product)
                .productName("Kopi Sachet")
                .price(new BigDecimal("3000"))
                .quantity(2)
                .subtotal(new BigDecimal("6000"))
                .build());
    }

    private String decode(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return text.startsWith("﻿") ? text.substring(1) : text; // strip UTF-8 BOM
    }

    @Test
    void generateSalesSummaryCsv_containsHeaderAndOneRowPerInvoice() {
        when(saleRepository.findBySaleDateBetweenOrderBySaleDateAsc(any(), any())).thenReturn(List.of(sale));

        String csv = decode(reportService.generateSalesSummaryCsv(LocalDate.now(), LocalDate.now()));

        assertThat(csv).contains("No Invoice", "Kasir", "Total");
        assertThat(csv).contains("INV-20260101-000001");
        assertThat(csv).contains("Budi Santoso");
        assertThat(csv).contains("6660");
    }

    @Test
    void generateSalesDetailCsv_containsOneRowPerSaleItem() {
        when(saleRepository.findBySaleDateBetweenOrderBySaleDateAsc(any(), any())).thenReturn(List.of(sale));

        String csv = decode(reportService.generateSalesDetailCsv(LocalDate.now(), LocalDate.now()));

        assertThat(csv).contains("Produk", "Qty");
        assertThat(csv).contains("Kopi Sachet");
        assertThat(csv).contains("INV-20260101-000001");
    }

    @Test
    void generateProductStockCsv_listsEachProductWithStockInfo() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        String csv = decode(reportService.generateProductStockCsv());

        assertThat(csv).contains("SKU", "Stok");
        assertThat(csv).contains("SKU-001");
        assertThat(csv).contains("Kopi Sachet");
        assertThat(csv).contains("Aktif");
    }
}
