package com.pos.receipt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pos.receipt.dto.ReceiptView;
import com.pos.sales.service.SaleService;
import com.pos.sales.dto.SaleItemResponse;
import com.pos.sales.dto.SaleResponse;
import com.pos.sales.entity.PaymentMethod;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private SaleService saleService;

    private ReceiptService receiptService;

    private UUID saleId;

    @BeforeEach
    void setUp() {
        receiptService = new ReceiptService(saleService);
        ReflectionTestUtils.setField(receiptService, "storeName", "Toko Sederhana");
        ReflectionTestUtils.setField(receiptService, "storeAddress", "Jl. Contoh No. 123, Jakarta");
        ReflectionTestUtils.setField(receiptService, "storePhone", "0812-3456-7890");

        saleId = UUID.randomUUID();
        SaleResponse sale = SaleResponse.builder()
                .id(saleId)
                .invoiceNumber("INV-20260101-000001")
                .saleDate(Instant.parse("2026-01-01T03:00:00Z"))
                .cashierName("Kasir Toko")
                .customerName("Budi Santoso")
                .items(List.of(SaleItemResponse.builder()
                        .productId(UUID.randomUUID())
                        .productName("Kopi Sachet")
                        .price(new BigDecimal("3000"))
                        .quantity(2)
                        .subtotal(new BigDecimal("6000"))
                        .build()))
                .subtotal(new BigDecimal("6000"))
                .discountAmount(new BigDecimal("0"))
                .taxAmount(new BigDecimal("660"))
                .total(new BigDecimal("6660"))
                .paymentMethod(PaymentMethod.CASH)
                .amountPaid(new BigDecimal("10000"))
                .changeAmount(new BigDecimal("3340"))
                .build();
        when(saleService.findById(saleId)).thenReturn(sale);
    }

    @Test
    void buildView_mapsSaleAndStoreInfoIntoReceiptView() {
        ReceiptView view = receiptService.buildView(saleId);

        assertThat(view.getStoreName()).isEqualTo("Toko Sederhana");
        assertThat(view.getInvoiceNumber()).isEqualTo("INV-20260101-000001");
        assertThat(view.getCashierName()).isEqualTo("Kasir Toko");
        assertThat(view.getItems()).hasSize(1);
        assertThat(view.getItems().get(0).getProductName()).isEqualTo("Kopi Sachet");
        assertThat(view.getTotal()).isEqualByComparingTo("6660");
    }

    @Test
    void buildEscPos_producesNonEmptyBytesStartingWithEscInitCommand() {
        byte[] bytes = receiptService.buildEscPos(saleId);

        assertThat(bytes).isNotEmpty();
        // ESC @ (0x1B 0x40) is the printer-initialize command every receipt must start with.
        assertThat(bytes[0]).isEqualTo((byte) 0x1B);
        assertThat(bytes[1]).isEqualTo((byte) 0x40);

        String rendered = new String(bytes, StandardCharsets.UTF_8);
        assertThat(rendered).contains("Toko Sederhana").contains("INV-20260101-000001").contains("Kopi Sachet");
    }
}
