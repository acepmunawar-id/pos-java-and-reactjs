package com.pos.product.mapper;

import com.pos.product.dto.UnitRequest;
import com.pos.product.dto.UnitResponse;
import com.pos.product.entity.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnitMapper {
    UnitResponse toResponse(Unit unit);

    Unit toEntity(UnitRequest request);

    void updateEntity(UnitRequest request, @MappingTarget Unit unit);
}
