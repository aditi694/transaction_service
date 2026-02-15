package com.bank.transaction_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Key key;

    private static final String SECRET =
            "BANKING_UNIFIED_SECRET_KEY_32_CHARACTERS_MINIMUM_LENGTH_2026";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        key = Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    @Test
    void parse_validToken_shouldReturnClaims() {
        String token = Jwts.builder()
                .setSubject("customer123")
                .claim("role", "ROLE_CUSTOMER")
                .setIssuedAt(new Date())
                .signWith(key)
                .compact();

        Claims claims = jwtUtil.parse(token);

        assertNotNull(claims);
        assertEquals("customer123", claims.getSubject());
        assertEquals("ROLE_CUSTOMER", claims.get("role"));
    }

    @Test
    void parse_invalidSignature_shouldThrowException() {
        Key wrongKey =
                Keys.hmacShaKeyFor("WRONG_SECRET_KEY_32_CHARACTERS_LONG_2026".getBytes());

        String token = Jwts.builder()
                .setSubject("customer123")
                .signWith(wrongKey)
                .compact();

        assertThrows(Exception.class,
                () -> jwtUtil.parse(token));
    }

    @Test
    void parse_malformedToken_shouldThrowException() {
        String invalidToken = "invalid.jwt.token";

        assertThrows(Exception.class,
                () -> jwtUtil.parse(invalidToken));
    }
}
