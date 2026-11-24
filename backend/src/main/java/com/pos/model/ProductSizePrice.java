package com.pos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Harga jual produk per takaran untuk satu ukuran kemasan. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSizePrice {

    @Column(name = "size_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal sizeAmount;

    @Column(name = "sell_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal sellPrice;
}
