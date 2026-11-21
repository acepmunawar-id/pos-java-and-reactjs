package com.pos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxRequest {
    @NotBlank(message = "Nama pajak wajib diisi")
    private String name;

    @NotNull(message = "Tarif pajak wajib diisi")
    @DecimalMin(value = "0", message = "Tarif tidak boleh negatif")
    @DecimalMax(value = "100", message = "Tarif tidak boleh lebih dari 100%")
    private BigDecimal rate;

    private boolean active = true;
}
