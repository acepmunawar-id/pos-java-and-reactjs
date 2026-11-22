package com.pos.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Rincian satu ukuran kemasan produk per takaran, termasuk modal & stok yang bisa dibuat. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSizeResponse {
    private BigDecimal sizeAmount;
    private BigDecimal sellPrice;
    private BigDecimal materialAmount;
    private BigDecimal mixerAmount;
    private BigDecimal packagingPrice;
    private BigDecimal unitCost;
    private BigDecimal profit;
    /** Jumlah kemasan yang masih bisa dibuat dari sisa bahan. */
    private int available;
}
