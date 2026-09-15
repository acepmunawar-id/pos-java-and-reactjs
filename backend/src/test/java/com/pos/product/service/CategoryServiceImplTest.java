package com.pos.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.CategoryRequest;
import com.pos.product.dto.CategoryResponse;
import com.pos.product.entity.Category;
import com.pos.product.mapper.CategoryMapper;
import com.pos.product.repository.CategoryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryServiceImpl categoryService;

    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);
        categoryService = new CategoryServiceImpl(categoryRepository, mapper);

        request = new CategoryRequest();
        request.setName("Minuman");
        request.setDescription("Kategori minuman kemasan");
    }

    @Test
    void create_savesCategory_whenNameNotTaken() {
        when(categoryRepository.existsByNameIgnoreCase("Minuman")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = categoryService.create(request);

        assertThat(response.getName()).isEqualTo("Minuman");
    }

    @Test
    void create_throwsDuplicateResourceException_whenNameAlreadyExists() {
        when(categoryRepository.existsByNameIgnoreCase("Minuman")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void findById_throwsResourceNotFound_whenCategoryMissing() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(id)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_throwsDuplicateResourceException_whenRenamedToExistingCategory() {
        UUID id = UUID.randomUUID();
        Category existing = Category.builder().id(id).name("Snack").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Minuman", id)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(id, request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_removesCategory_whenFound() {
        UUID id = UUID.randomUUID();
        Category existing = Category.builder().id(id).name("Minuman").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));

        categoryService.delete(id);

        org.mockito.Mockito.verify(categoryRepository).delete(existing);
    }
}
