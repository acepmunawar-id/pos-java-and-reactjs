package com.pos.dto;

import com.pos.model.CategoryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "Nama kategori wajib diisi")
    private String name;

    private String description;

    private CategoryType type = CategoryType.GENERAL;

    // ---- Hanya untuk type = MEASURED ----
    private String baseUnit;

    @DecimalMin(value = "0", message = "Rasio bahan utama tidak boleh negatif")
    private BigDecimal materialRatio;

    @DecimalMin(value = "0", message = "Rasio campuran tidak boleh negatif")
    private BigDecimal mixerRatio;

    private String mixerName;

    @DecimalMin(value = "0", message = "Harga campuran tidak boleh negatif")
    private BigDecimal mixerPricePerUnit;

    @DecimalMin(value = "0", message = "Stok campuran tidak boleh negatif")
    private BigDecimal mixerStock;

    @Valid
    private List<CategorySizeDto> sizes = new ArrayList<>();
}
