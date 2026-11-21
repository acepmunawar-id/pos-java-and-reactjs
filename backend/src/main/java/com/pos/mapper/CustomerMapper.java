package com.pos.mapper;

import com.pos.dto.PartnerRequest;
import com.pos.dto.PartnerResponse;
import com.pos.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    PartnerResponse toResponse(Customer customer);

    Customer toEntity(PartnerRequest request);

    void updateEntity(PartnerRequest request, @MappingTarget Customer customer);
}
