package com.pos.pricing.dto;

import com.pos.pricing.entity.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountRequest {
    @NotBlank(message = "Nama diskon wajib diisi")
    private String name;

    @NotNull(message = "Tipe diskon wajib dipilih")
    private DiscountType type;

    @NotNull(message = "Nilai diskon wajib diisi")
    @DecimalMin(value = "0", message = "Nilai diskon tidak boleh negatif")
    private BigDecimal value;

    private boolean active = true;
}
