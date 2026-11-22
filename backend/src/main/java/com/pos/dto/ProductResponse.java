package com.pos.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String sku;
    private String barcode;
    private String name;
    private CategoryResponse category;
    private UnitResponse unit;
    private BigDecimal purchasePrice;
    private BigDecimal sellPrice;
    private int stock;
    private int minStock;
    private boolean active;
    private boolean lowStock;

    // ---- Produk per takaran ----
    private boolean measured;
    private BigDecimal materialPricePerUnit;
    private BigDecimal materialStock;
    private BigDecimal minMaterialStock;
    private List<ProductSizeResponse> sizes;
}
