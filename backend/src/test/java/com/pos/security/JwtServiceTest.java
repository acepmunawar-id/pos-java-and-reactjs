package com.pos.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pos.user.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Exercises the real JwtService (other tests mock it, which once hid a secret-decoding bug). */
class JwtServiceTest {

    // Same shape as the default secret in application.yml: plain text containing dashes.
    private static final String SECRET = "change-this-super-secret-key-please-override-in-env-min-32-bytes";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = newService(SECRET, 3_600_000L);
        user = User.builder().username("kasir").password("x").fullName("Kasir").role(Role.USER).active(true).build();
    }

    private static JwtService newService(String secret, long expirationMs) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "expirationMs", expirationMs);
        return service;
    }

    @Test
    void generateToken_thenExtract_roundTripsUsernameAndRole() {
        String token = jwtService.generateToken(user, Role.USER);

        assertThat(jwtService.extractUsername(token)).isEqualTo("kasir");
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.USER);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_forDifferentUser() {
        String token = jwtService.generateToken(user, Role.USER);
        User other = User.builder().username("admin").password("x").fullName("A").role(Role.ADMIN).active(true).build();

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void extractUsername_throws_whenTokenSignedWithDifferentSecret() {
        String token = newService("another-secret-key-which-is-at-least-32-bytes-long!!", 3_600_000L)
                .generateToken(user, Role.USER);

        assertThatThrownBy(() -> jwtService.extractUsername(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void extractUsername_throws_whenTokenExpired() {
        String token = newService(SECRET, -1000L).generateToken(user, Role.USER);

        assertThatThrownBy(() -> jwtService.extractUsername(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void generateToken_failsFast_whenSecretShorterThan32Bytes() {
        JwtService weak = newService("too-short", 3_600_000L);

        assertThatThrownBy(() -> weak.generateToken(user, Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
