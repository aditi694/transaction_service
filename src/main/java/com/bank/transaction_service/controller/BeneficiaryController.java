package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.util.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<BaseResponse<BeneficiaryResponse>> add(
            @RequestBody @Valid BeneficiaryRequest request
    ) {
        AuthUser user = getAuthUser();
        request.setCustomerId(user.getCustomerId().toString());

        BeneficiaryResponse response = beneficiaryService.add(request);

        String msg = response.getVerificationStatus().equals("VERIFIED")
                ? AppConstants.BENEFICIARY_VERIFIED_MSG
                : AppConstants.BENEFICIARY_PENDING_MSG;

        return ResponseEntity.ok(
                BaseResponse.success(response, msg)
        );
    }


    @GetMapping
    public ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> listOwn() {
        AuthUser user = getAuthUser();

        List<BeneficiaryResponse> list =
                beneficiaryService.list(user.getCustomerId().toString());

        return ResponseEntity.ok(
                BaseResponse.success(
                        list,
                        list.isEmpty()
                                ? "No beneficiaries found"
                                : "Beneficiaries fetched successfully"
                )
        );
    }


    private AuthUser getAuthUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null ||
                !auth.isAuthenticated() ||
                !(auth.getPrincipal() instanceof AuthUser)) {

            throw new AccessDeniedException(AppConstants.UNAUTHORIZED);
        }

        return (AuthUser) auth.getPrincipal();
    }
}
