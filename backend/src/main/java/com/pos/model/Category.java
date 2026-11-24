package com.pos.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "sizes")
public class Category extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private CategoryType type = CategoryType.GENERAL;

    // ---- Pengaturan per takaran (hanya dipakai bila type = MEASURED) ----

    /** Satuan dasar stok & ukuran kemasan, mis. "ml" atau "gram". */
    @Column(name = "base_unit", length = 10)
    private String baseUnit;

    /** Bagian bahan utama dalam rasio campuran, mis. 2 pada rasio 2 : 1. */
    @Column(name = "material_ratio", precision = 10, scale = 2)
    private BigDecimal materialRatio;

    /** Bagian bahan campuran dalam rasio, mis. 1 pada rasio 2 : 1. Kosong/0 = tanpa campuran. */
    @Column(name = "mixer_ratio", precision = 10, scale = 2)
    private BigDecimal mixerRatio;

    /** Nama bahan campuran, mis. "Alkohol Absolut". */
    @Column(name = "mixer_name", length = 50)
    private String mixerName;

    @Column(name = "mixer_price_per_unit", precision = 19, scale = 4)
    private BigDecimal mixerPricePerUnit;

    /** Stok bahan campuran (satuan dasar) - dipakai bersama semua produk di kategori ini. */
    @Builder.Default
    @Column(name = "mixer_stock", nullable = false, precision = 19, scale = 3)
    private BigDecimal mixerStock = BigDecimal.ZERO;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "category_sizes", joinColumns = @JoinColumn(name = "category_id"))
    @OrderBy("sizeAmount asc")
    private List<CategorySize> sizes = new ArrayList<>();

    public boolean isMeasured() {
        return type == CategoryType.MEASURED;
    }

    /** true bila kategori memakai bahan campuran (rasio campuran diisi & lebih dari 0). */
    public boolean hasMixer() {
        return isMeasured()
                && mixerRatio != null && mixerRatio.signum() > 0
                && materialRatio != null && materialRatio.signum() > 0;
    }

    /** Takaran bahan utama untuk satu kemasan berisi {@code sizeAmount}. */
    public BigDecimal materialAmountFor(BigDecimal sizeAmount) {
        return materialAmountFor(sizeAmount, 3);
    }

    /** Takaran bahan campuran untuk satu kemasan berisi {@code sizeAmount} (0 bila tanpa campuran). */
    public BigDecimal mixerAmountFor(BigDecimal sizeAmount) {
        return mixerAmountFor(sizeAmount, 3);
    }

    /** Versi dengan presisi tertentu - dipakai perhitungan modal agar tidak terkena pembulatan takaran. */
    public BigDecimal materialAmountFor(BigDecimal sizeAmount, int scale) {
        if (!hasMixer()) {
            return sizeAmount;
        }
        return sizeAmount.multiply(materialRatio)
                .divide(materialRatio.add(mixerRatio), scale, RoundingMode.HALF_UP);
    }

    public BigDecimal mixerAmountFor(BigDecimal sizeAmount, int scale) {
        return hasMixer() ? sizeAmount.subtract(materialAmountFor(sizeAmount, scale)) : BigDecimal.ZERO;
    }

    public Optional<CategorySize> findSize(BigDecimal sizeAmount) {
        return sizes.stream().filter(s -> s.getSizeAmount().compareTo(sizeAmount) == 0).findFirst();
    }
}
