package com.pos.service;

import com.pos.dto.DiscountRequest;
import com.pos.dto.DiscountResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiscountService {
    Page<DiscountResponse> findAll(String search, Pageable pageable);

    List<DiscountResponse> findAllActive();

    DiscountResponse findById(UUID id);

    DiscountResponse create(DiscountRequest request);

    DiscountResponse update(UUID id, DiscountRequest request);

    void delete(UUID id);
}
