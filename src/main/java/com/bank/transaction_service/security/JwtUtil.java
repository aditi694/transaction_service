package com.bank.transaction_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.UUID;

@Component
public class JwtUtil {

    // 🔥 SAME SECRET IN ALL SERVICES
    private static final String SECRET = "BANKING_UNIFIED_SECRET_KEY_32_CHARACTERS_MINIMUM_LENGTH_2026";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public AuthUser parse(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        UUID customerId = UUID.fromString(claims.get("customerId", String.class));
        String role = claims.get("role", String.class);

        return new AuthUser(customerId, role);
    }
}