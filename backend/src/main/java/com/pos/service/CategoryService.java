package com.pos.service;

import com.pos.dto.CategoryRequest;
import com.pos.dto.CategoryResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<CategoryResponse> findAll(String search, Pageable pageable);

    CategoryResponse findById(UUID id);

    CategoryResponse create(CategoryRequest request);

    CategoryResponse update(UUID id, CategoryRequest request);

    void delete(UUID id);
}
