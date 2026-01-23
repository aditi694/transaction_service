package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping("/beneficiaries/{beneficiaryId}/approve")
    public BeneficiaryResponse approveBeneficiary(@PathVariable String beneficiaryId) {
        AuthUser admin = getAuthUser();

        if (!admin.isAdmin()) {
            throw TransactionException.unauthorized("Admin access required");
        }

        beneficiaryService.adminVerify(beneficiaryId, admin.getCustomerId());
        return beneficiaryService.get(beneficiaryId);
    }

    @GetMapping("/beneficiaries")
    public List<BeneficiaryResponse> getAllBeneficiaries(
            @RequestParam(required = false) String customerId,
            @RequestParam(defaultValue = "false") boolean pendingOnly
    ) {
        AuthUser admin = getAuthUser();

        if (!admin.isAdmin()) {
            throw TransactionException.unauthorized("Admin access required");
        }

        if (customerId != null) {
            return beneficiaryService.list(customerId);
        }

        if (pendingOnly) {
            return beneficiaryService.listPendingApprovals();
        }

        return beneficiaryService.listAll();
    }

    @PostMapping("/beneficiaries/{beneficiaryId}/reject")
    public BeneficiaryResponse rejectBeneficiary(@PathVariable String beneficiaryId) {
        AuthUser admin = getAuthUser();

        if (!admin.isAdmin()) {
            throw TransactionException.unauthorized("Admin access required");
        }

        beneficiaryService.reject(beneficiaryId);
        return beneficiaryService.get(beneficiaryId);
    }

    private AuthUser getAuthUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();

        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }

        throw TransactionException.unauthorized("User not authenticated");
    }
}