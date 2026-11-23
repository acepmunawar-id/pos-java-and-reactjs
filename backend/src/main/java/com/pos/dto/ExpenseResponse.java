package com.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        LocalDate expenseDate,
        UUID categoryId,
        String categoryName,
        String description,
        BigDecimal amount,
        UUID supplierId,
        String supplierName
) {}
