package com.pos.pricing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.pricing.dto.DiscountResponse;
import com.pos.pricing.entity.DiscountType;
import com.pos.pricing.service.DiscountService;
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

@PosControllerTest(DiscountController.class)
class DiscountControllerTest extends AbstractControllerTest {

    @MockBean
    private DiscountService discountService;

    private String validPayload() {
        return """
                { "name": "Diskon Member 10%", "type": "PERCENTAGE", "value": 10, "active": true }
                """;
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forCashierRole() throws Exception {
        when(discountService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/discounts")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAllActive_returns200_forCashierRole() throws Exception {
        when(discountService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/discounts/active")).andExpect(status().isOk());
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/discounts")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdminRole() throws Exception {
        when(discountService.create(any())).thenReturn(
                DiscountResponse.builder().id(UUID.randomUUID()).name("Diskon Member 10%")
                        .type(DiscountType.PERCENTAGE).value(new BigDecimal("10")).active(true).build());

        mockMvc.perform(post("/api/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns400_whenTypeMissing() throws Exception {
        mockMvc.perform(post("/api/discounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Diskon\", \"value\": 10 }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_returns404_whenDiscountMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(discountService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Diskon", id));

        mockMvc.perform(get("/api/discounts/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_returns403_forCashierRole() throws Exception {
        mockMvc.perform(delete("/api/discounts/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/discounts/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
