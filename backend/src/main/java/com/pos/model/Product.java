package com.pos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"category", "unit", "sizePrices"})
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(length = 100)
    private String barcode;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Column(name = "purchase_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "sell_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal sellPrice;

    @Column(nullable = false)
    private int stock;

    @Column(name = "min_stock", nullable = false)
    private int minStock;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // ---- Khusus produk di kategori PER TAKARAN ----

    /** Harga bahan utama per satuan dasar (per ml / per gram). */
    @Column(name = "material_price_per_unit", precision = 19, scale = 4)
    private BigDecimal materialPricePerUnit;

    /** Stok bahan utama dalam satuan dasar kategori (ml / gram). */
    @Builder.Default
    @Column(name = "material_stock", nullable = false, precision = 19, scale = 3)
    private BigDecimal materialStock = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "min_material_stock", nullable = false, precision = 19, scale = 3)
    private BigDecimal minMaterialStock = BigDecimal.ZERO;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_size_prices", joinColumns = @JoinColumn(name = "product_id"))
    private List<ProductSizePrice> sizePrices = new ArrayList<>();

    public boolean isMeasured() {
        return category != null && category.isMeasured();
    }

    public Optional<BigDecimal> sellPriceFor(BigDecimal sizeAmount) {
        return sizePrices.stream()
                .filter(p -> p.getSizeAmount().compareTo(sizeAmount) == 0)
                .map(ProductSizePrice::getSellPrice)
                .findFirst();
    }

    /** Modal satu kemasan: bahan utama + bahan campuran + kemasan kosong. */
    public BigDecimal unitCostFor(BigDecimal sizeAmount) {
        BigDecimal material = category.materialAmountFor(sizeAmount, 10).multiply(orZero(materialPricePerUnit));
        BigDecimal mixer = category.mixerAmountFor(sizeAmount, 10).multiply(orZero(category.getMixerPricePerUnit()));
        BigDecimal packaging = category.findSize(sizeAmount).map(CategorySize::getPackagingPrice).orElse(BigDecimal.ZERO);
        return material.add(mixer).add(packaging).setScale(2, RoundingMode.HALF_UP);
    }

    /** Berapa kemasan ukuran ini yang masih bisa dibuat dari sisa bahan utama & campuran. */
    public int availableFor(BigDecimal sizeAmount) {
        BigDecimal materialNeed = category.materialAmountFor(sizeAmount);
        int byMaterial = materialStock.signum() <= 0 ? 0
                : materialStock.divide(materialNeed, 0, RoundingMode.FLOOR).intValue();
        if (!category.hasMixer()) {
            return byMaterial;
        }
        BigDecimal mixerStock = category.getMixerStock();
        int byMixer = mixerStock.signum() <= 0 ? 0
                : mixerStock.divide(category.mixerAmountFor(sizeAmount), 0, RoundingMode.FLOOR).intValue();
        return Math.min(byMaterial, byMixer);
    }

    public boolean isLowStock() {
        return isMeasured()
                ? materialStock.compareTo(minMaterialStock) <= 0
                : stock <= minStock;
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
