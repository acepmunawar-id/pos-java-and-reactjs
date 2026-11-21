package com.pos.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReceiptItemView {
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
