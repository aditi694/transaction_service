package com.bank.transaction_service.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class AuthUser extends AbstractAuthenticationToken {

    private final UUID customerId;
    private final String role;

    public AuthUser(UUID customerId, String role) {
        super(getAuthorities(role));
        this.customerId = customerId;
        this.role = role;
        setAuthenticated(true);
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public Object getPrincipal() {
        return customerId;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        // Check both with and without ROLE_ prefix
        return "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isCustomer() {
        return "ROLE_CUSTOMER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role);
    }
}
