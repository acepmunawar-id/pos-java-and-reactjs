package com.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DebtPaymentRequest {

    @NotNull(message = "ID transaksi wajib diisi")
    private UUID saleId;

    @NotNull(message = "Jumlah bayar wajib diisi")
    @DecimalMin(value = "0.01", message = "Jumlah bayar harus lebih dari 0")
    private BigDecimal amount;

    private String notes;
}
