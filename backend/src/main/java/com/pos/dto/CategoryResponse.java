package com.pos.dto;

import com.pos.model.CategoryType;
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
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private CategoryType type;
    private String baseUnit;
    private BigDecimal materialRatio;
    private BigDecimal mixerRatio;
    private String mixerName;
    private BigDecimal mixerPricePerUnit;
    private BigDecimal mixerStock;
    private boolean hasMixer;
    private List<CategorySizeDto> sizes;
}
