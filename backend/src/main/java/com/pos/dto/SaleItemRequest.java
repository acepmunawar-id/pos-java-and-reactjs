package com.pos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleItemRequest {

    @NotNull(message = "Produk wajib dipilih")
    private UUID productId;

    @Min(value = 1, message = "Kuantitas minimal 1")
    private int quantity;

    /** Wajib untuk produk per takaran: ukuran kemasan yang dijual (ml / gram). */
    private BigDecimal sizeAmount;
}
