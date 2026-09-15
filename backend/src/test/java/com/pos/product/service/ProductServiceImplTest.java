package com.pos.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.ProductRequest;
import com.pos.product.dto.ProductResponse;
import com.pos.product.entity.Product;
import com.pos.product.mapper.CategoryMapper;
import com.pos.product.mapper.ProductMapper;
import com.pos.product.mapper.UnitMapper;
import com.pos.product.repository.CategoryRepository;
import com.pos.product.repository.ProductRepository;
import com.pos.product.repository.UnitRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnitRepository unitRepository;

    private ProductServiceImpl productService;

    private ProductRequest request;

    @BeforeEach
    void setUp() {
        // ProductMapper delegates to CategoryMapper/UnitMapper via Spring injection at runtime;
        // outside a Spring context we wire the generated collaborators in manually.
        ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);
        ReflectionTestUtils.setField(productMapper, "categoryMapper", Mappers.getMapper(CategoryMapper.class));
        ReflectionTestUtils.setField(productMapper, "unitMapper", Mappers.getMapper(UnitMapper.class));
        productService = new ProductServiceImpl(productRepository, categoryRepository, unitRepository, productMapper);

        request = new ProductRequest();
        request.setSku("SKU-001");
        request.setName("Kopi Sachet");
        request.setPurchasePrice(new BigDecimal("2000"));
        request.setSellPrice(new BigDecimal("3000"));
        request.setStock(50);
        request.setMinStock(10);
        request.setActive(true);
    }

    @Test
    void create_savesProduct_whenSkuNotTaken() {
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.getSku()).isEqualTo("SKU-001");
        assertThat(response.isLowStock()).isFalse();
    }

    @Test
    void create_throwsDuplicateResourceException_whenSkuAlreadyExists() {
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_throwsResourceNotFound_whenCategoryIdDoesNotExist() {
        UUID categoryId = UUID.randomUUID();
        request.setCategoryId(categoryId);
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void mapper_marksLowStock_whenStockAtOrBelowMinimum() {
        when(productRepository.existsBySkuIgnoreCase("SKU-001")).thenReturn(false);
        request.setStock(5);
        request.setMinStock(10);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.isLowStock()).isTrue();
    }
}
