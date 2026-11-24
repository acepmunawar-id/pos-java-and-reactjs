package com.pos.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Hutang seorang pelanggan beserta daftar transaksi yang belum lunas. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDebtResponse {

    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalDebt;
    private List<DebtSaleRow> sales;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DebtSaleRow {
        private UUID saleId;
        private String invoiceNumber;
        private Instant saleDate;
        private BigDecimal total;
        private BigDecimal totalPaid;
        private BigDecimal remaining;
    }
}
