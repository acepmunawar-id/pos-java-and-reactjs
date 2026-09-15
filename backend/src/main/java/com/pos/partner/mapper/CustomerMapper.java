package com.pos.partner.mapper;

import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    PartnerResponse toResponse(Customer customer);

    Customer toEntity(PartnerRequest request);

    void updateEntity(PartnerRequest request, @MappingTarget Customer customer);
}
