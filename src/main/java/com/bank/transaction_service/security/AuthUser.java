package com.bank.transaction_service.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;
import java.util.UUID;

public class AuthUser extends AbstractAuthenticationToken {

    private final UUID customerId;
    private final String role;

    public AuthUser(UUID customerId, String role) {
        super(Collections.emptyList());
        this.customerId = customerId;
        this.role = role;
        setAuthenticated(true);
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
}
