package com.pos.dto;

import com.pos.model.PaymentMethod;
import com.pos.model.SaleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private UUID id;
    private String invoiceNumber;
    private Instant saleDate;
    private UUID customerId;
    private String customerName;
    private UUID cashierId;
    private String cashierName;
    private String taxName;
    private String discountName;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private SaleStatus status;
    private List<SaleItemResponse> items;
}
