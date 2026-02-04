package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.util.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.bank.transaction_service.util.AppConstants.*;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> add(@RequestBody @Valid BeneficiaryRequest request) {
        AuthUser user = getAuthUser();
        request.setCustomerId(user.getCustomerId().toString());

        BeneficiaryResponse response = beneficiaryService.add(request);

        // ✅ Use AppConstants
        String message = response.isVerified()
                ? BENEFICIARY_VERIFIED_MSG
                : BENEFICIARY_PENDING_MSG;

        return ResponseEntity.ok(Map.of(
                SUCCESS, true,
                MESSAGE, message,
                DATA, response
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listOwn() {
        AuthUser user = getAuthUser();
        List<BeneficiaryResponse> list = beneficiaryService.list(user.getCustomerId().toString());

        return ResponseEntity.ok(Map.of(
                SUCCESS, true,
                MESSAGE, list.isEmpty() ? "No beneficiaries found" : "Beneficiaries fetched successfully",
                "count", list.size(),
                "beneficiaries", list
        ));
    }

    private AuthUser getAuthUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AuthUser)) {
            throw new RuntimeException("User not authenticated"); // Will be replaced in exception phase
        }
        return (AuthUser) auth.getPrincipal();
    }
}