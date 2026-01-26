package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
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
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody BeneficiaryRequest request) {
        AuthUser user = getAuthUser();
        request.setCustomerId(user.getCustomerId().toString());
        BeneficiaryResponse response = beneficiaryService.add(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response.isVerified()
                        ? "Beneficiary added and verified"
                        : "Beneficiary added, pending verification",
                "data", response
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listOwn() {
        AuthUser user = getAuthUser();
        List<BeneficiaryResponse> list =
                beneficiaryService.list(user.getCustomerId().toString());

        if (list.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "No beneficiaries found",
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

    private AuthUser getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthUser)) {
            throw TransactionException.unauthorized("User not authenticated");
        }

        return (AuthUser) auth.getPrincipal();
    }
}
