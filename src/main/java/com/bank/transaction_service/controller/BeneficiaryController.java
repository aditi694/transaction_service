package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    /**
     * Add new beneficiary - Customer only
     */
    @PostMapping
    public BeneficiaryResponse add(@RequestBody BeneficiaryRequest request) {
        AuthUser user = getAuthUser();

        // Set customerId from authenticated user
        request.setCustomerId(user.getCustomerId().toString());

        return beneficiaryService.add(request);
    }

    /**
     * List all beneficiaries for logged-in customer
     */
    @GetMapping
    public List<BeneficiaryResponse> list() {
        AuthUser user = getAuthUser();
        return beneficiaryService.list(user.getCustomerId().toString());
    }

    /**
     * Verify beneficiary - Can be customer initiated but admin approved
     * For now, allowing customer to verify
     */
    @PostMapping("/{id}/verify")
    public BeneficiaryResponse verify(@PathVariable String id) {
        AuthUser user = getAuthUser();

        // Optional: Check if this beneficiary belongs to the customer
        beneficiaryService.verify(id);

        return beneficiaryService.get(id);
    }

    /**
     * Admin approval for beneficiary verification (future enhancement)
     */
    @PostMapping("/admin/{id}/approve")
    public BeneficiaryResponse adminApprove(@PathVariable String id) {
        AuthUser user = getAuthUser();

        if (!user.isAdmin()) {
            throw TransactionException.unauthorized("Admin access required");
        }

        beneficiaryService.verify(id);
        return beneficiaryService.get(id);
    }

    private AuthUser getAuthUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();

        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }

        throw TransactionException.unauthorized("User not authenticated");
    }
}