package com.pos.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pos.auth.dto.LoginRequest;
import com.pos.auth.dto.LoginResponse;
import com.pos.security.JwtService;
import com.pos.security.Role;
import com.pos.user.entity.User;
import com.pos.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("kasir");
        loginRequest.setPassword("Password123!");

        user = User.builder()
                .id(UUID.randomUUID())
                .username("kasir")
                .password("hashed")
                .fullName("Kasir Toko")
                .role(Role.USER)
                .active(true)
                .build();
    }

    @Test
    void login_returnsTokenAndRole_whenCredentialsValid() {
        when(userRepository.findByUsername("kasir")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user, Role.USER)).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getUsername()).isEqualTo("kasir");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_throwsBadCredentials_whenAuthenticationManagerRejects() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_throwsUsernameNotFound_whenUserMissingAfterAuthentication() {
        when(userRepository.findByUsername("kasir")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest)).isInstanceOf(UsernameNotFoundException.class);
    }
}
