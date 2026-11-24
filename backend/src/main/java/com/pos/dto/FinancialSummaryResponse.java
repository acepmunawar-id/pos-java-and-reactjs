package com.pos.dto;

import java.math.BigDecimal;

public record FinancialSummaryResponse(
        BigDecimal totalMasuk,
        BigDecimal totalKeluar,
        BigDecimal selisih,
        BigDecimal saldo
) {}
