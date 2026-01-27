package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/limits")
@RequiredArgsConstructor
public class TransactionLimitController {

    private final TransactionLimitService limitService;

    @GetMapping
    public TransactionLimitResponse getLimits(@RequestParam String accountNumber) {
        AuthUser user = getAuthUser();
        return limitService.get(accountNumber);
    }

    @PutMapping
    public TransactionLimitResponse updateLimits(
            @RequestParam String accountNumber,
            @RequestBody LimitUpdateRequest request
    ) {
        AuthUser user = getAuthUser();
        return limitService.update(accountNumber, request);
    }

    private AuthUser getAuthUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw TransactionException.unauthorized("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthUser authUser) {
            return authUser;
        }

        throw TransactionException.unauthorized("Invalid authentication principal");
    }
}
