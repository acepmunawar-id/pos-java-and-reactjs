package com.pos.mapper;

import com.pos.dto.CategoryResponse;
import com.pos.dto.CategorySizeDto;
import com.pos.model.Category;
import com.pos.model.CategorySize;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "hasMixer", expression = "java(category.hasMixer())")
    CategoryResponse toResponse(Category category);

    CategorySizeDto toSizeDto(CategorySize size);
}
