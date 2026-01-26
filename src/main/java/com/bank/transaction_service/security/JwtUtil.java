package com.bank.transaction_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtil {
    private static final String SECRET = "BANKING_UNIFIED_SECRET_KEY_32_CHARACTERS_MINIMUM_LENGTH_2026";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
