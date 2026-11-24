package com.pos.service;

import com.pos.exception.DuplicateResourceException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.dto.ProductRequest;
import com.pos.dto.ProductResponse;
import com.pos.model.Category;
import com.pos.model.Product;
import com.pos.model.Unit;
import com.pos.dto.ProductSizePriceDto;
import com.pos.exception.BadRequestException;
import com.pos.mapper.ProductMapper;
import com.pos.model.ProductSizePrice;
import java.math.BigDecimal;
import com.pos.repository.CategoryRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.UnitRepository;
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
    public Page<ProductResponse> findAll(String search, UUID categoryId, Pageable pageable) {
        boolean hasSearch   = search     != null && !search.isBlank();
        boolean hasCategory = categoryId != null;
        Page<Product> page;
        if (hasSearch && hasCategory)  page = productRepository.findAllActiveBySearchAndCategory(search, categoryId, pageable);
        else if (hasSearch)            page = productRepository.findAllActiveBySearch(search, pageable);
        else if (hasCategory)          page = productRepository.findAllActiveByCategoryId(categoryId, pageable);
        else                           page = productRepository.findAllActive(pageable);
        return page.map(productMapper::toResponse);
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
        Product product = new Product();
        apply(request, product);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getOrThrow(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("SKU '" + request.getSku() + "' sudah digunakan");
        }
        apply(request, product);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void delete(UUID id) {
        productRepository.delete(getOrThrow(id));
    }

    /**
     * Salin isi request ke entity.
     * <ul>
     *   <li>Kategori UMUM: harga beli, harga jual, stok & stok minimum dari request.</li>
     *   <li>Kategori PER TAKARAN: harga bahan per satuan, stok bahan, dan harga jual
     *       per ukuran kemasan. Harga beli/jual di produk diisi otomatis dari ukuran
     *       terkecil (untuk tampilan ringkas & laporan), stok pcs tidak dipakai.</li>
     * </ul>
     */
    private void apply(ProductRequest request, Product product) {
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setName(request.getName());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setUnit(resolveUnit(request.getUnitId()));
        product.setActive(request.isActive());

        if (!product.isMeasured()) {
            product.setPurchasePrice(request.getPurchasePrice());
            product.setSellPrice(request.getSellPrice());
            product.setStock(request.getStock());
            product.setMinStock(request.getMinStock());
            product.setMaterialPricePerUnit(null);
            product.setMaterialStock(BigDecimal.ZERO);
            product.setMinMaterialStock(BigDecimal.ZERO);
            product.getSizePrices().clear();
            return;
        }

        Category category = product.getCategory();
        if (request.getMaterialPricePerUnit() == null) {
            throw new BadRequestException("Harga bahan per " + category.getBaseUnit() + " wajib diisi");
        }
        product.setMaterialPricePerUnit(request.getMaterialPricePerUnit());
        product.setMaterialStock(orZero(request.getMaterialStock()));
        product.setMinMaterialStock(orZero(request.getMinMaterialStock()));
        product.setStock(0);
        product.setMinStock(0);

        product.getSizePrices().clear();
        for (ProductSizePriceDto price : request.getSizePrices() == null ? List.<ProductSizePriceDto>of() : request.getSizePrices()) {
            if (category.findSize(price.getSizeAmount()).isEmpty()) {
                throw new BadRequestException("Ukuran " + price.getSizeAmount().stripTrailingZeros().toPlainString() + " "
                        + category.getBaseUnit() + " tidak ada di kategori " + category.getName());
            }
            if (product.sellPriceFor(price.getSizeAmount()).isPresent()) {
                throw new BadRequestException("Harga untuk ukuran " + price.getSizeAmount().stripTrailingZeros().toPlainString() + " dobel");
            }
            product.getSizePrices().add(new ProductSizePrice(price.getSizeAmount(), price.getSellPrice()));
        }
        if (product.getSizePrices().isEmpty()) {
            throw new BadRequestException("Isi harga jual minimal untuk satu ukuran kemasan");
        }

        BigDecimal smallest = product.getSizePrices().stream()
                .map(ProductSizePrice::getSizeAmount).min(BigDecimal::compareTo).orElseThrow();
        product.setSellPrice(product.sellPriceFor(smallest).orElseThrow());
        product.setPurchasePrice(product.unitCostFor(smallest));
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
