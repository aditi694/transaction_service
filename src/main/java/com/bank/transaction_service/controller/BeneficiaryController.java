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


    @PostMapping
    public BeneficiaryResponse add(@RequestBody BeneficiaryRequest request) {
        AuthUser user = getAuthUser();
        request.setCustomerId(user.getCustomerId().toString());
        return beneficiaryService.add(request);
    }

    @GetMapping
    public List<BeneficiaryResponse> listOwn() {
        AuthUser user = getAuthUser();
        return beneficiaryService.list(user.getCustomerId().toString());
    }

    @GetMapping("/admin/all")
    public List<BeneficiaryResponse> listAll() {
        requireAdmin();
        return beneficiaryService.listAll();
    }

    @GetMapping("/admin/pending")
    public List<BeneficiaryResponse> pending() {
        requireAdmin();
        return beneficiaryService.listPendingApprovals();
    }

    @PostMapping("/admin/{id}/approve")
    public void approve(@PathVariable String id) {
        AuthUser admin = requireAdmin();
        beneficiaryService.adminVerify(id, admin.getCustomerId());
    }

    @PostMapping("/admin/{id}/reject")
    public void reject(@PathVariable String id) {
        requireAdmin();
        beneficiaryService.reject(id);
    }

    private AuthUser getAuthUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof AuthUser au) return au;
        throw TransactionException.unauthorized("User not authenticated");
    }

    private AuthUser requireAdmin() {
        AuthUser user = getAuthUser();
        if (!user.isAdmin()) {
            throw TransactionException.unauthorized("Admin access required");
        }
        return user;
    }
}
