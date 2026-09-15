package com.pos.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.common.exception.DuplicateResourceException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.security.Role;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import com.pos.user.dto.UserResponse;
import com.pos.user.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

/** Whole controller is SUPER_ADMIN-only, unlike the other master-data controllers. */
@PosControllerTest(UserController.class)
class UserControllerTest extends AbstractControllerTest {

    @MockBean
    private UserService userService;

    private String validPayload() {
        return """
                {
                  "username": "newadmin",
                  "password": "Password123!",
                  "fullName": "New Admin",
                  "role": "ADMIN",
                  "active": true
                }
                """;
    }

    @Test
    void findAll_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_returns403_forAdminRole() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void findAll_returns403_forCashierRole() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void findAll_returns200_forSuperAdminRole() throws Exception {
        when(userService.findAll(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void create_returns200_forSuperAdminRole() throws Exception {
        when(userService.create(any())).thenReturn(
                UserResponse.builder().id(UUID.randomUUID()).username("newadmin").fullName("New Admin").role(Role.ADMIN).active(true).build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void create_returns400_whenUsernameBlank() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"\", \"password\": \"Password123!\", \"fullName\": \"X\", \"role\": \"ADMIN\" }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void create_returns409_whenUsernameAlreadyTaken() throws Exception {
        when(userService.create(any())).thenThrow(new DuplicateResourceException("Username 'newadmin' sudah digunakan"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void findById_returns404_whenUserMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findById(eq(id))).thenThrow(ResourceNotFoundException.of("User", id));

        mockMvc.perform(get("/api/users/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_returns200_forSuperAdminRole() throws Exception {
        mockMvc.perform(delete("/api/users/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}
