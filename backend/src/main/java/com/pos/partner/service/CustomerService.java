package com.pos.partner.service;

import com.pos.partner.dto.PartnerRequest;
import com.pos.partner.dto.PartnerResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<PartnerResponse> findAll(String search, Pageable pageable);

    PartnerResponse findById(UUID id);

    PartnerResponse create(PartnerRequest request);

    PartnerResponse update(UUID id, PartnerRequest request);

    void delete(UUID id);
}
