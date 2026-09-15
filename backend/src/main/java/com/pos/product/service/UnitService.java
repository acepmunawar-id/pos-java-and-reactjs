package com.pos.product.service;

import com.pos.product.dto.UnitRequest;
import com.pos.product.dto.UnitResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UnitService {
    Page<UnitResponse> findAll(String search, Pageable pageable);

    UnitResponse findById(UUID id);

    UnitResponse create(UnitRequest request);

    UnitResponse update(UUID id, UnitRequest request);

    void delete(UUID id);
}
