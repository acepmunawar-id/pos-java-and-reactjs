package com.pos.mapper;

import com.pos.dto.ProductResponse;
import com.pos.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UnitMapper.class})
public interface ProductMapper {

    @Mapping(target = "lowStock", expression = "java(product.getStock() <= product.getMinStock())")
    ProductResponse toResponse(Product product);
}
