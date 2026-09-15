package com.pos.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.CategoryResponse;
import com.pos.product.service.CategoryService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(CategoryController.class)
class CategoryControllerTest extends AbstractControllerTest {

    @MockBean
    private CategoryService categoryService;

    private String validPayload() {
        return """
                { "name": "Minuman", "description": "Kategori minuman kemasan" }
                """;
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forAnyAuthenticatedRole() throws Exception {
        when(categoryService.findAll(any(), any())).thenReturn(new PageImpl<>(java.util.List.of()));

        mockMvc.perform(get("/api/categories")).andExpect(status().isOk());
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdminRole() throws Exception {
        when(categoryService.create(any())).thenReturn(
                CategoryResponse.builder().id(UUID.randomUUID()).name("Minuman").build());

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns400_whenNameBlank() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns409_whenNameAlreadyExists() throws Exception {
        when(categoryService.create(any())).thenThrow(new DuplicateResourceException("Kategori 'Minuman' sudah ada"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_returns404_whenCategoryMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Kategori", id));

        mockMvc.perform(get("/api/categories/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_returns403_forCashierRole() throws Exception {
        mockMvc.perform(delete("/api/categories/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/categories/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
