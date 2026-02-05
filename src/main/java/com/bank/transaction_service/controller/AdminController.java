package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/beneficiaries")
@RequiredArgsConstructor
public class AdminController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> list(
            @RequestParam(defaultValue = "false") boolean pendingOnly
    ) {
        requireAdmin();

        List<BeneficiaryResponse> list = pendingOnly
                ? beneficiaryService.listPendingApprovals()
                : beneficiaryService.listAll();

        String message = pendingOnly
                ? (list.isEmpty()
                ? "No pending beneficiary approvals"
                : "Pending beneficiaries fetched")
                : (list.isEmpty()
                ? "No beneficiaries found"
                : "All beneficiaries fetched");

        return ResponseEntity.ok(
                BaseResponse.success(list, message)
        );

    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BaseResponse<Void>> approve(@PathVariable String id) {
        requireAdmin();

        beneficiaryService.adminVerify(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, AppConstants.BENEFICIARY_VERIFIED_MSG)
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BaseResponse<Void>> reject(@PathVariable String id) {
        requireAdmin();

        beneficiaryService.reject(id);

        return ResponseEntity.ok(
                BaseResponse.success(null, AppConstants.BENEFICIARY_REJECTED_MSG)
        );
    }

    private void requireAdmin() {
        AuthUser user = (AuthUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!user.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    AppConstants.FORBIDDEN
            );
        }
    }
}
