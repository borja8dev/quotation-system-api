package com.quotation.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256-algorithm-ok";
    private static final long ACCESS_EXPIRY = 900000L;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(provider, "accessTokenExpiry", ACCESS_EXPIRY);
    }

    @Test
    void generateAccessToken_WhenCalled_ThenReturnsNonEmptyToken() {
        String token = provider.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_WhenValidToken_ThenReturnsTrue() {
        String token = provider.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        assertTrue(provider.validateToken(token));
    }

    @Test
    void validateToken_WhenTamperedToken_ThenReturnsFalse() {
        String token = provider.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(provider.validateToken(tampered));
    }

    @Test
    void validateToken_WhenExpiredToken_ThenReturnsFalse() {
        JwtTokenProvider shortLived = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortLived, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(shortLived, "accessTokenExpiry", -1000L);

        String token = shortLived.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        assertFalse(shortLived.validateToken(token));
    }

    @Test
    void validateToken_WhenWrongSecret_ThenReturnsFalse() {
        JwtTokenProvider other = new JwtTokenProvider();
        ReflectionTestUtils.setField(other, "jwtSecret", "completely-different-secret-key-at-least-32-chars");
        ReflectionTestUtils.setField(other, "accessTokenExpiry", ACCESS_EXPIRY);

        String token = other.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        assertFalse(provider.validateToken(token));
    }

    @Test
    void getClaims_WhenValidToken_ThenExtractsUserIdAndRoles() {
        String token = provider.generateAccessToken(42L, List.of("ROLE_ADMIN"));
        Claims claims = provider.getClaims(token);

        assertEquals("42", claims.getSubject());
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void generateAccessToken_WhenCalled_ThenTokenHasJtiClaim() {
        String token = provider.generateAccessToken(1L, List.of("ROLE_CLIENT"));
        Claims claims = provider.getClaims(token);
        assertNotNull(claims.getId());
        assertFalse(claims.getId().isBlank());
    }
}
