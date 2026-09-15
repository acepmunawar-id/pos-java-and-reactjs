package com.pos.sales.dto;

import java.math.BigDecimal;
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
public class SaleItemResponse {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
}
