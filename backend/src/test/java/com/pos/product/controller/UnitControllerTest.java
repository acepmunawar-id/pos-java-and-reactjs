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
import com.pos.product.dto.UnitResponse;
import com.pos.product.service.UnitService;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@PosControllerTest(UnitController.class)
class UnitControllerTest extends AbstractControllerTest {

    @MockBean
    private UnitService unitService;

    private String validPayload() {
        return """
                { "name": "Pieces", "symbol": "pcs" }
                """;
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns200_forAnyAuthenticatedRole() throws Exception {
        when(unitService.findAll(any(), any())).thenReturn(new PageImpl<>(java.util.List.of()));

        mockMvc.perform(get("/api/units")).andExpect(status().isOk());
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/units")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_returns403_forCashierRole() throws Exception {
        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns200_forAdminRole() throws Exception {
        when(unitService.create(any())).thenReturn(
                UnitResponse.builder().id(UUID.randomUUID()).name("Pieces").symbol("pcs").build());

        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns400_whenSymbolBlank() throws Exception {
        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Pieces\", \"symbol\": \"\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns409_whenNameAlreadyExists() throws Exception {
        when(unitService.create(any())).thenThrow(new DuplicateResourceException("Satuan 'Pieces' sudah ada"));

        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_returns404_whenUnitMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(unitService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("Satuan", id));

        mockMvc.perform(get("/api/units/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void delete_returns403_forCashierRole() throws Exception {
        mockMvc.perform(delete("/api/units/" + UUID.randomUUID())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/units/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
