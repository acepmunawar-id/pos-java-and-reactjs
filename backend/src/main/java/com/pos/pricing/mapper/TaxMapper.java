package com.pos.pricing.mapper;

import com.pos.pricing.dto.TaxRequest;
import com.pos.pricing.dto.TaxResponse;
import com.pos.pricing.entity.Tax;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaxMapper {
    TaxResponse toResponse(Tax tax);

    Tax toEntity(TaxRequest request);

    void updateEntity(TaxRequest request, @MappingTarget Tax tax);
}
