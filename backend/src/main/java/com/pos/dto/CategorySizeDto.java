package com.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ukuran kemasan kategori per takaran (request & response). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySizeDto {

    @NotNull(message = "Ukuran kemasan wajib diisi")
    @DecimalMin(value = "0.01", message = "Ukuran kemasan harus lebih dari 0")
    private BigDecimal sizeAmount;

    @DecimalMin(value = "0", message = "Harga kemasan tidak boleh negatif")
    private BigDecimal packagingPrice = BigDecimal.ZERO;
}
