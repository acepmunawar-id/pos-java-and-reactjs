package com.pos.partner.mapper;

import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    PartnerResponse toResponse(Supplier supplier);

    Supplier toEntity(PartnerRequest request);

    void updateEntity(PartnerRequest request, @MappingTarget Supplier supplier);
}
