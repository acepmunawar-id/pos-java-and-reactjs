package com.pos.service;

import com.pos.dto.ProductRequest;
import com.pos.dto.ProductResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> findAll(String search, Pageable pageable);

    List<ProductResponse> findLowStock();

    ProductResponse findById(UUID id);

    ProductResponse findByBarcode(String barcode);

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);
}
