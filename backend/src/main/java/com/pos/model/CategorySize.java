package com.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Satu ukuran kemasan pada kategori per takaran, mis. 500 ml dengan harga botol Rp2.000. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySize {

    /** Isi kemasan dalam satuan dasar kategori (ml / gram). */
    @Column(name = "size_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal sizeAmount;

    /** Harga kemasan kosong (botol, plastik, toples). */
    @Column(name = "packaging_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal packagingPrice;
}
