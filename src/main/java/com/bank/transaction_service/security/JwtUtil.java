package com.bank.transaction_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyRep;
import java.util.UUID;

import static java.security.KeyRep.Type.SECRET;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthUser parse(String token) {

        Claims claims = Jwts.parser()
                .setSigningKey(String.valueOf(KeyRep.Type.SECRET))
                .parseClaimsJws(token)
                .getBody();

        UUID customerId = UUID.fromString(claims.get("customerId", String.class));
        String role = claims.get("role", String.class);

        return new AuthUser(customerId, role);
    }
}
