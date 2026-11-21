package com.pos.mapper;

import com.pos.dto.PartnerRequest;
import com.pos.dto.PartnerResponse;
import com.pos.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    PartnerResponse toResponse(Supplier supplier);

    Supplier toEntity(PartnerRequest request);

    void updateEntity(PartnerRequest request, @MappingTarget Supplier supplier);
}
