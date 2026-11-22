package com.pos.mapper;

import com.pos.dto.ProductResponse;
import com.pos.dto.ProductSizeResponse;
import com.pos.model.CategorySize;
import com.pos.model.Product;
import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UnitMapper.class})
public interface ProductMapper {

    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    @Mapping(target = "measured", expression = "java(product.isMeasured())")
    @Mapping(target = "sizes", expression = "java(toSizes(product))")
    ProductResponse toResponse(Product product);

    /**
     * Satu baris per ukuran kemasan kategori yang punya harga jual di produk ini,
     * lengkap dengan takaran bahan, modal, untung, dan jumlah yang masih bisa dibuat.
     */
    default List<ProductSizeResponse> toSizes(Product product) {
        if (!product.isMeasured()) {
            return List.of();
        }
        var category = product.getCategory();
        return category.getSizes().stream()
                .map(CategorySize::getSizeAmount)
                .filter(size -> product.sellPriceFor(size).isPresent())
                .map(size -> {
                    BigDecimal price = product.sellPriceFor(size).orElseThrow();
                    BigDecimal cost = product.unitCostFor(size);
                    return ProductSizeResponse.builder()
                            .sizeAmount(size)
                            .sellPrice(price)
                            .materialAmount(category.materialAmountFor(size))
                            .mixerAmount(category.mixerAmountFor(size))
                            .packagingPrice(category.findSize(size).map(CategorySize::getPackagingPrice).orElse(BigDecimal.ZERO))
                            .unitCost(cost)
                            .profit(price.subtract(cost))
                            .available(product.availableFor(size))
                            .build();
                })
                .toList();
    }
}
