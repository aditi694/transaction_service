package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.bank.transaction_service.util.AppConstants.*;

@RestController
@RequestMapping("/api/admin/beneficiaries")
@RequiredArgsConstructor
public class AdminController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "false") boolean pendingOnly) {
        requireAdmin();

        List<BeneficiaryResponse> list = pendingOnly
                ? beneficiaryService.listPendingApprovals()
                : beneficiaryService.listAll();

        String message = pendingOnly
                ? (list.isEmpty() ? "No pending beneficiary approvals" : "Pending beneficiaries fetched")
                : (list.isEmpty() ? "No beneficiaries found" : "All beneficiaries fetched");

        return ResponseEntity.ok(Map.of(
                SUCCESS, true,
                MESSAGE, message,
                "count", list.size(),
                "beneficiaries", list
        ));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable String id) {
        AuthUser admin = requireAdmin();
        beneficiaryService.adminVerify(id, admin.getCustomerId());

        return ResponseEntity.ok(Map.of(
                SUCCESS, true,
                MESSAGE, BENEFICIARY_VERIFIED_MSG
        ));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String id) {
        requireAdmin();
        beneficiaryService.reject(id);

        return ResponseEntity.ok(Map.of(
                SUCCESS, true,
                MESSAGE, BENEFICIARY_REJECTED_MSG
        ));
    }

    private AuthUser requireAdmin() {
        // Same as before, will replace exception later
        AuthUser user = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.isAdmin()) {
            throw new RuntimeException("Admin access required");
        }
        return user;
    }
}