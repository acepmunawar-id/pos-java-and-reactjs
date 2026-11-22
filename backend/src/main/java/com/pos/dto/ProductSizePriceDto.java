package com.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Harga jual produk per takaran untuk satu ukuran kemasan (request). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSizePriceDto {

    @NotNull(message = "Ukuran kemasan wajib diisi")
    private BigDecimal sizeAmount;

    @NotNull(message = "Harga jual per ukuran wajib diisi")
    @DecimalMin(value = "0", message = "Harga jual tidak boleh negatif")
    private BigDecimal sellPrice;
}
