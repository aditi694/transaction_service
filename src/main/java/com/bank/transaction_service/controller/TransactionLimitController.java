package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionLimitService;
import com.bank.transaction_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/limits")
@RequiredArgsConstructor
public class TransactionLimitController {

    private final TransactionLimitService limitService;

    @GetMapping
    public ResponseEntity<BaseResponse<TransactionLimitResponse>> getLimits(
            @RequestParam String accountNumber
    ) {
        getAuthUser();

        TransactionLimitResponse response =
                limitService.get(accountNumber);

        return ResponseEntity.ok(
                BaseResponse.success(
                        response,
                        "Transaction limits fetched successfully"
                )
        );
    }

    @PutMapping
    public ResponseEntity<BaseResponse<TransactionLimitResponse>> updateLimits(
            @RequestParam String accountNumber,
            @RequestBody LimitUpdateRequest request
    ) {
        getAuthUser();

        TransactionLimitResponse response =
                limitService.update(accountNumber, request);

        return ResponseEntity.ok(
                BaseResponse.success(
                        response,
                        "Transaction limits updated successfully"
                )
        );
    }

    private AuthUser getAuthUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof AuthUser)) {

            throw new AccessDeniedException(AppConstants.UNAUTHORIZED);
        }

        return (AuthUser) authentication.getPrincipal();
    }
}
