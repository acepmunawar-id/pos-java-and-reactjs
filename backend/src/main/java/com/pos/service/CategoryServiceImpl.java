package com.pos.service;

import com.pos.exception.DuplicateResourceException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.dto.CategoryRequest;
import com.pos.dto.CategoryResponse;
import com.pos.model.Category;
import com.pos.dto.CategorySizeDto;
import com.pos.exception.BadRequestException;
import com.pos.mapper.CategoryMapper;
import com.pos.model.CategorySize;
import com.pos.model.CategoryType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.pos.repository.CategoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(String search, Pageable pageable) {
        String keyword = search == null ? "" : search;
        return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable).map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(UUID id) {
        return categoryMapper.toResponse(getOrThrow(id));
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Kategori '" + request.getName() + "' sudah ada");
        }
        Category category = new Category();
        apply(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = getOrThrow(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("Kategori '" + request.getName() + "' sudah ada");
        }
        apply(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(UUID id) {
        categoryRepository.delete(getOrThrow(id));
    }

    /**
     * Salin isi request ke entity. Untuk kategori PER TAKARAN, satuan dasar & minimal
     * satu ukuran kemasan wajib; bahan campuran opsional (rasio campuran kosong/0 =
     * tanpa campuran). Kategori UMUM tidak menyimpan pengaturan takaran.
     */
    private void apply(CategoryRequest request, Category category) {
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        CategoryType type = request.getType() == null ? CategoryType.GENERAL : request.getType();
        category.setType(type);

        if (type == CategoryType.GENERAL) {
            category.setBaseUnit(null);
            category.setMaterialRatio(null);
            category.setMixerRatio(null);
            category.setMixerName(null);
            category.setMixerPricePerUnit(null);
            category.setMixerStock(BigDecimal.ZERO);
            category.getSizes().clear();
            return;
        }

        if (request.getBaseUnit() == null || request.getBaseUnit().isBlank()) {
            throw new BadRequestException("Satuan dasar (mis. ml / gram) wajib diisi untuk kategori per takaran");
        }
        List<CategorySizeDto> sizes = request.getSizes() == null ? List.of() : request.getSizes();
        if (sizes.isEmpty()) {
            throw new BadRequestException("Kategori per takaran minimal punya satu ukuran kemasan");
        }
        Set<BigDecimal> seen = new TreeSet<>();
        for (CategorySizeDto size : sizes) {
            if (!seen.add(size.getSizeAmount().stripTrailingZeros())) {
                throw new BadRequestException("Ukuran kemasan " + size.getSizeAmount().stripTrailingZeros().toPlainString() + " dobel");
            }
        }

        category.setBaseUnit(request.getBaseUnit().trim());
        boolean withMixer = request.getMixerRatio() != null && request.getMixerRatio().signum() > 0;
        if (withMixer) {
            if (request.getMaterialRatio() == null || request.getMaterialRatio().signum() <= 0) {
                throw new BadRequestException("Rasio bahan utama wajib lebih dari 0 bila memakai bahan campuran");
            }
            category.setMaterialRatio(request.getMaterialRatio());
            category.setMixerRatio(request.getMixerRatio());
            category.setMixerName(request.getMixerName());
            category.setMixerPricePerUnit(request.getMixerPricePerUnit() == null ? BigDecimal.ZERO : request.getMixerPricePerUnit());
            category.setMixerStock(request.getMixerStock() == null ? BigDecimal.ZERO : request.getMixerStock());
        } else {
            category.setMaterialRatio(null);
            category.setMixerRatio(null);
            category.setMixerName(null);
            category.setMixerPricePerUnit(null);
            category.setMixerStock(BigDecimal.ZERO);
        }

        category.getSizes().clear();
        sizes.forEach(size -> category.getSizes().add(new CategorySize(
                size.getSizeAmount(),
                size.getPackagingPrice() == null ? BigDecimal.ZERO : size.getPackagingPrice())));
    }

    private Category getOrThrow(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Kategori", id));
    }
}
