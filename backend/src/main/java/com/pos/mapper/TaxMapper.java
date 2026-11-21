package com.pos.mapper;

import com.pos.dto.TaxRequest;
import com.pos.dto.TaxResponse;
import com.pos.model.Tax;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaxMapper {
    TaxResponse toResponse(Tax tax);

    Tax toEntity(TaxRequest request);

    void updateEntity(TaxRequest request, @MappingTarget Tax tax);
}
