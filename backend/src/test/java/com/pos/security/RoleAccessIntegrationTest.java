package com.pos.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * End-to-end verification of the role matrix enforced via {@code @PreAuthorize}:
 * SUPER_ADMIN has full access, ADMIN manages master data but not users,
 * USER (cashier) can read master data and transact but not manage it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousRequest_isRejected() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void cashier_canReadProducts_butCannotCreateThem() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isOk());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canManageMasterData() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_cannotManageUsers() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdmin_canManageUsers() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void cashier_cannotAccessReports() throws Exception {
        mockMvc.perform(get("/api/reports/products/stock.csv")).andExpect(status().isForbidden());
    }

    private String validProductJson() {
        return """
                {
                  "sku": "SKU-TEST-001",
                  "name": "Produk Uji",
                  "purchasePrice": 1000,
                  "sellPrice": 1500,
                  "stock": 10,
                  "minStock": 2,
                  "active": true
                }
                """;
    }
}
