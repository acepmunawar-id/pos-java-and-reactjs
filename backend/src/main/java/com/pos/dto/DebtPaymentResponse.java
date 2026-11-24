package com.pos.dto;

import java.math.BigDecimal;
import java.time.Instant;
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
public class DebtPaymentResponse {
    private UUID id;
    private UUID saleId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String notes;
    private String cashierName;
    private Instant paidAt;
}
