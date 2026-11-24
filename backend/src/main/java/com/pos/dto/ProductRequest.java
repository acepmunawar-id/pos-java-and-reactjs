package com.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "SKU wajib diisi")
    private String sku;

    private String barcode;

    @NotBlank(message = "Nama produk wajib diisi")
    private String name;

    private String imageUrl;

    private UUID categoryId;

    private UUID unitId;

    @NotNull(message = "Harga beli wajib diisi")
    @DecimalMin(value = "0", message = "Harga beli tidak boleh negatif")
    private BigDecimal purchasePrice;

    @NotNull(message = "Harga jual wajib diisi")
    @DecimalMin(value = "0", message = "Harga jual tidak boleh negatif")
    private BigDecimal sellPrice;

    @Min(value = 0, message = "Stok tidak boleh negatif")
    private int stock;

    @Min(value = 0, message = "Stok minimum tidak boleh negatif")
    private int minStock;

    private boolean active = true;

    // ---- Hanya untuk produk di kategori PER TAKARAN ----
    @DecimalMin(value = "0", message = "Harga bahan tidak boleh negatif")
    private BigDecimal materialPricePerUnit;

    @DecimalMin(value = "0", message = "Stok bahan tidak boleh negatif")
    private BigDecimal materialStock;

    @DecimalMin(value = "0", message = "Stok bahan minimum tidak boleh negatif")
    private BigDecimal minMaterialStock;

    @Valid
    private List<ProductSizePriceDto> sizePrices = new ArrayList<>();
}
