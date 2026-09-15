package com.pos.pricing.service;

import com.pos.pricing.dto.TaxRequest;
import com.pos.pricing.dto.TaxResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaxService {
    Page<TaxResponse> findAll(String search, Pageable pageable);

    List<TaxResponse> findAllActive();

    TaxResponse findById(UUID id);

    TaxResponse create(TaxRequest request);

    TaxResponse update(UUID id, TaxRequest request);

    void delete(UUID id);
}
