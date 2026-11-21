package com.pos.mapper;

import com.pos.dto.UnitRequest;
import com.pos.dto.UnitResponse;
import com.pos.model.Unit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnitMapper {
    UnitResponse toResponse(Unit unit);

    Unit toEntity(UnitRequest request);

    void updateEntity(UnitRequest request, @MappingTarget Unit unit);
}
