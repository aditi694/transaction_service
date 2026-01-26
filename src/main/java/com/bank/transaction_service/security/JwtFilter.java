package com.bank.transaction_service.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/internal/")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Claims claims = jwtUtil.parse(token);

                UUID customerId =
                        UUID.fromString(claims.get("customerId", String.class));

                String role = claims.get("role", String.class);
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                AuthUser authUser = new AuthUser(customerId, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authUser,
                                null,
                                authUser.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Invalid token\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
