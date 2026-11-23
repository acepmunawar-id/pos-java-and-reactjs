package com.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseRequest(

        @NotNull(message = "Tanggal wajib diisi")
        LocalDate expenseDate,

        @NotNull(message = "Kategori wajib diisi")
        UUID categoryId,

        String description,

        @NotNull(message = "Jumlah wajib diisi")
        @DecimalMin(value = "0.0", inclusive = false, message = "Jumlah harus lebih dari 0")
        BigDecimal amount,

        UUID supplierId
) {}
