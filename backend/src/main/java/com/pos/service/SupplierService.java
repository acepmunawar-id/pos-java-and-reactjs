package com.pos.service;

import com.pos.dto.PartnerRequest;
import com.pos.dto.PartnerResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Page<PartnerResponse> findAll(String search, Pageable pageable);

    PartnerResponse findById(UUID id);

    PartnerResponse create(PartnerRequest request);

    PartnerResponse update(UUID id, PartnerRequest request);

    void delete(UUID id);
}
