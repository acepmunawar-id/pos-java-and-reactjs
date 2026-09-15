package com.pos.pricing.mapper;

import com.pos.pricing.dto.DiscountRequest;
import com.pos.pricing.dto.DiscountResponse;
import com.pos.pricing.entity.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    DiscountResponse toResponse(Discount discount);

    Discount toEntity(DiscountRequest request);

    void updateEntity(DiscountRequest request, @MappingTarget Discount discount);
}
