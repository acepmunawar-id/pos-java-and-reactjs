package com.pos.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pos.auth.dto.LoginRequest;
import com.pos.auth.dto.LoginResponse;
import com.pos.auth.service.AuthService;
import com.pos.security.Role;
import com.pos.testsupport.AbstractControllerTest;
import com.pos.testsupport.PosControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

@PosControllerTest(AuthController.class)
class AuthControllerTest extends AbstractControllerTest {

    @MockBean
    private AuthService authService;

    @Test
    void login_returns200WithToken_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("superadmin");
        request.setPassword("Password123!");

        when(authService.login(any())).thenReturn(LoginResponse.builder()
                .token("fake-jwt")
                .username("superadmin")
                .fullName("Super Administrator")
                .role(Role.SUPER_ADMIN)
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("fake-jwt"))
                .andExpect(jsonPath("$.data.role").value("SUPER_ADMIN"));
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("superadmin");
        request.setPassword("wrong-password");

        when(authService.login(any())).thenThrow(new BadCredentialsException("invalid"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns400_whenUsernameBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
