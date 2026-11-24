package com.pos.mapper;

import com.pos.dto.DiscountRequest;
import com.pos.dto.DiscountResponse;
import com.pos.model.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    DiscountResponse toResponse(Discount discount);

    Discount toEntity(DiscountRequest request);

    void updateEntity(DiscountRequest request, @MappingTarget Discount discount);
}
