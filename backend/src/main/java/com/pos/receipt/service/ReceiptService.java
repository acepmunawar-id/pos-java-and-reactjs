package com.pos.receipt.service;

import com.pos.receipt.dto.ReceiptItemView;
import com.pos.receipt.dto.ReceiptView;
import com.pos.sales.service.SaleService;
import com.pos.sales.dto.SaleItemResponse;
import com.pos.sales.dto.SaleResponse;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Builds receipt/bill representations of a sale: a structured {@link ReceiptView}
 * for the frontend's printable-HTML fallback, and raw ESC/POS bytes for direct
 * thermal printing (frontend sends these over the Web Serial API).
 */
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final int LINE_WIDTH = 32; // safe default for 58mm printers; still fits 80mm
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SaleService saleService;

    @Value("${app.store.name}")
    private String storeName;

    @Value("${app.store.address}")
    private String storeAddress;

    @Value("${app.store.phone}")
    private String storePhone;

    public ReceiptView buildView(UUID saleId) {
        SaleResponse sale = saleService.findById(saleId);
        return ReceiptView.builder()
                .storeName(storeName)
                .storeAddress(storeAddress)
                .storePhone(storePhone)
                .invoiceNumber(sale.getInvoiceNumber())
                .saleDate(sale.getSaleDate())
                .cashierName(sale.getCashierName())
                .customerName(sale.getCustomerName())
                .items(sale.getItems().stream()
                        .map(i -> ReceiptItemView.builder()
                                .productName(i.getProductName())
                                .quantity(i.getQuantity())
                                .price(i.getPrice())
                                .subtotal(i.getSubtotal())
                                .build())
                        .toList())
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .total(sale.getTotal())
                .paymentMethod(sale.getPaymentMethod().name())
                .amountPaid(sale.getAmountPaid())
                .changeAmount(sale.getChangeAmount())
                .build();
    }

    public byte[] buildEscPos(UUID saleId) {
        SaleResponse sale = saleService.findById(saleId);
        EscPosBuilder builder = new EscPosBuilder()
                .init()
                .alignCenter()
                .bold(true)
                .line(storeName)
                .bold(false)
                .line(storeAddress)
                .line(storePhone)
                .divider(LINE_WIDTH)
                .alignLeft()
                .line("No: " + sale.getInvoiceNumber())
                .line("Tgl: " + DATE_FORMAT.withZone(ZoneId.systemDefault()).format(sale.getSaleDate()))
                .line("Kasir: " + sale.getCashierName())
                .divider(LINE_WIDTH);

        for (SaleItemResponse item : sale.getItems()) {
            builder.line(item.getProductName());
            builder.twoColumns(
                    item.getQuantity() + " x " + formatMoney(item.getPrice()),
                    formatMoney(item.getSubtotal()),
                    LINE_WIDTH);
        }

        builder.divider(LINE_WIDTH)
                .twoColumns("Subtotal", formatMoney(sale.getSubtotal()), LINE_WIDTH);
        if (sale.getDiscountAmount() != null && sale.getDiscountAmount().signum() > 0) {
            builder.twoColumns("Diskon", "-" + formatMoney(sale.getDiscountAmount()), LINE_WIDTH);
        }
        if (sale.getTaxAmount() != null && sale.getTaxAmount().signum() > 0) {
            builder.twoColumns("Pajak", formatMoney(sale.getTaxAmount()), LINE_WIDTH);
        }
        builder.bold(true)
                .twoColumns("TOTAL", formatMoney(sale.getTotal()), LINE_WIDTH)
                .bold(false)
                .twoColumns("Bayar (" + sale.getPaymentMethod() + ")", formatMoney(sale.getAmountPaid()), LINE_WIDTH)
                .twoColumns("Kembali", formatMoney(sale.getChangeAmount()), LINE_WIDTH)
                .divider(LINE_WIDTH)
                .alignCenter()
                .line("Terima kasih atas kunjungan Anda")
                .cut();

        return builder.build();
    }

    private String formatMoney(BigDecimal value) {
        return "Rp" + value.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
