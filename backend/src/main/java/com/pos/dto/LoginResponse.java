package com.pos.dto;

import com.pos.security.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Returned to the frontend after a successful login. {@code role} is what the
 * SPA uses to decide which landing page/layout to redirect the user to.
 */
@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UUID userId;
    private String username;
    private String fullName;
    private Role role;
}
