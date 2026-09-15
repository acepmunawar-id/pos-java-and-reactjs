package com.pos.partner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.ResourceNotFoundException;
import com.pos.partner.dto.PartnerResponse;
import com.pos.partner.service.CustomerService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(CustomerController.class)
class CustomerControllerTest extends AbstractControllerTest {

    @MockBean
    private CustomerService customerService;

    private String validPayload() {
        return """
                { "name": "Budi Santoso", "phone": "0812-0000-0000" }
                """;
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forCashierRole() throws Exception {
        when(customerService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/customers")).andExpect(status().isOk());
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/customers")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdminRole() throws Exception {
        when(customerService.create(any())).thenReturn(
                PartnerResponse.builder().id(UUID.randomUUID()).name("Budi Santoso").build());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns400_whenNameBlank() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_returns404_whenCustomerMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Pelanggan", id));

        mockMvc.perform(get("/api/customers/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_returns403_forCashierRole() throws Exception {
        mockMvc.perform(delete("/api/customers/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/customers/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
