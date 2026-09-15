package com.pos.pricing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.pricing.dto.TaxResponse;
import com.pos.pricing.service.TaxService;
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

@PosControllerTest(TaxController.class)
class TaxControllerTest extends AbstractControllerTest {

    @MockBean
    private TaxService taxService;

    private String validPayload() {
        return """
                { "name": "PPN 11%", "rate": 11, "active": true }
                """;
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forCashierRole() throws Exception {
        when(taxService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/taxes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAllActive_returns200_forCashierRole() throws Exception {
        when(taxService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/taxes/active")).andExpect(status().isOk());
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/taxes")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/taxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdminRole() throws Exception {
        when(taxService.create(any())).thenReturn(
                TaxResponse.builder().id(UUID.randomUUID()).name("PPN 11%").rate(new BigDecimal("11")).active(true).build());

        mockMvc.perform(post("/api/taxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns400_whenRateExceeds100() throws Exception {
        mockMvc.perform(post("/api/taxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"PPN\", \"rate\": 150 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_returns404_whenTaxMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(taxService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Pajak", id));

        mockMvc.perform(get("/api/taxes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_returns403_forCashierRole() throws Exception {
        mockMvc.perform(delete("/api/taxes/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/taxes/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
