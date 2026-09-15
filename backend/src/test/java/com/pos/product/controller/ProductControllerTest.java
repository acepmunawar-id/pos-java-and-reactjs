package com.pos.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.product.dto.ProductResponse;
import com.pos.product.service.ProductService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(ProductController.class)
class ProductControllerTest extends AbstractControllerTest {

    @MockBean
    private ProductService productService;

    private String validPayload() {
        return """
                {
                  "sku": "SKU-001",
                  "name": "Kopi Sachet",
                  "purchasePrice": 2000,
                  "sellPrice": 3000,
                  "stock": 50,
                  "minStock": 10,
                  "active": true
                }
                """;
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forCashierRole() throws Exception {
        when(productService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findLowStock_returns200_forCashierRole() throws Exception {
        when(productService.findLowStock()).thenReturn(List.of());

        mockMvc.perform(get("/api/products/low-stock")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findByBarcode_returns200_whenProductFound() throws Exception {
        when(productService.findByBarcode("8991234567890")).thenReturn(
                ProductResponse.builder().id(UUID.randomUUID()).sku("SKU-001").name("Kopi Sachet").build());

        mockMvc.perform(get("/api/products/barcode/8991234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("SKU-001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void findByBarcode_returns404_whenProductMissing() throws Exception {
        when(productService.findByBarcode("0000")).thenThrow(new ResourceNotFoundException("Produk dengan barcode '0000' tidak ditemukan"));

        mockMvc.perform(get("/api/products/barcode/0000")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdminRole() throws Exception {
        when(productService.create(any())).thenReturn(
                ProductResponse.builder().id(UUID.randomUUID()).sku("SKU-001").name("Kopi Sachet")
                        .purchasePrice(new BigDecimal("2000")).sellPrice(new BigDecimal("3000")).build());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns400_whenSkuBlank() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"sku\": \"\", \"name\": \"Kopi\", \"purchasePrice\": 1, \"sellPrice\": 2 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns409_whenSkuAlreadyExists() throws Exception {
        when(productService.create(any())).thenThrow(new DuplicateResourceException("SKU 'SKU-001' sudah digunakan"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_returns403_forCashierRole() throws Exception {
        mockMvc.perform(delete("/api/products/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/products/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
