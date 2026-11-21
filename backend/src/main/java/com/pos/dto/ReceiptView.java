package com.pos.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Structured, print-ready view of a sale used by the frontend's HTML/browser
 * fallback receipt (80mm layout + window.print()) when no thermal printer /
 * Web Serial is available.
 */
@Getter
@Builder
@AllArgsConstructor
public class ReceiptView {
    private String storeName;
    private String storeAddress;
    private String storePhone;
    private String invoiceNumber;
    private Instant saleDate;
    private String cashierName;
    private String customerName;
    private List<ReceiptItemView> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
}
