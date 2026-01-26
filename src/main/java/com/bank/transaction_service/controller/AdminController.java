package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/beneficiaries")
@RequiredArgsConstructor
public class AdminController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "false") boolean pendingOnly
    ) {
        requireAdmin();

        List<BeneficiaryResponse> list = pendingOnly
                ? beneficiaryService.listPendingApprovals()
                : beneficiaryService.listAll();

        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", pendingOnly
                            ? "No pending beneficiary approvals"
                            : "No beneficiaries found",
                    "count", 0,
                    "beneficiaries", list
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Beneficiaries fetched successfully",
                "count", list.size(),
                "beneficiaries", list
        ));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable String id) {
        AuthUser admin = requireAdmin();
        beneficiaryService.adminVerify(id, admin.getCustomerId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Beneficiary approved"
        ));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String id) {
        requireAdmin();
        beneficiaryService.reject(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Beneficiary rejected"
        ));
    }

    private AuthUser requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthUser)) {
            throw TransactionException.unauthorized("User not authenticated");
        }

        AuthUser user = (AuthUser) auth.getPrincipal();

        if (!user.isAdmin()) {
            throw TransactionException.unauthorized("Admin access required");
        }

        return user;
    }
}
