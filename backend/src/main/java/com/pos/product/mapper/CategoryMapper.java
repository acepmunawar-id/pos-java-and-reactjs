package com.pos.product.mapper;

import com.pos.product.dto.CategoryRequest;
import com.pos.product.dto.CategoryResponse;
import com.pos.product.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);

    Category toEntity(CategoryRequest request);

    void updateEntity(CategoryRequest request, @MappingTarget Category category);
}
