package com.pos.product.service;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.ProductRequest;
import com.pos.product.dto.ProductResponse;
import com.pos.product.entity.Category;
import com.pos.product.entity.Product;
import com.pos.product.entity.Unit;
import com.pos.product.mapper.ProductMapper;
import com.pos.product.repository.CategoryRepository;
import com.pos.product.repository.ProductRepository;
import com.pos.product.repository.UnitRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrBarcodeContainingIgnoreCase(
                        keyword, keyword, keyword, pageable)
                .map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findLowStock() {
        return productRepository.findLowStockProducts().stream().map(productMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return productMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findByBarcode(String barcode) {
        return productMapper.toResponse(
                productRepository
                        .findByBarcode(barcode)
                        .orElseThrow(() -> new ResourceNotFoundException("Produk dengan barcode '" + barcode + "' tidak ditemukan")));
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new DuplicateResourceException("SKU '" + request.getSku() + "' sudah digunakan");
        }
        Product product = Product.builder()
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .name(request.getName())
                .category(resolveCategory(request.getCategoryId()))
                .unit(resolveUnit(request.getUnitId()))
                .purchasePrice(request.getPurchasePrice())
                .sellPrice(request.getSellPrice())
                .stock(request.getStock())
                .minStock(request.getMinStock())
                .active(request.isActive())
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getOrThrow(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("SKU '" + request.getSku() + "' sudah digunakan");
        }
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setName(request.getName());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setUnit(resolveUnit(request.getUnitId()));
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSellPrice(request.getSellPrice());
        product.setStock(request.getStock());
        product.setMinStock(request.getMinStock());
        product.setActive(request.isActive());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void delete(UUID id) {
        productRepository.delete(getOrThrow(id));
    }

    private Product getOrThrow(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Produk", id));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Kategori", categoryId));
    }

    private Unit resolveUnit(UUID unitId) {
        if (unitId == null) {
            return null;
        }
        return unitRepository.findById(unitId).orElseThrow(() -> ResourceNotFoundException.of("Satuan", unitId));
    }
}
