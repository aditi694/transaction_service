package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/debit")
    public DebitTransactionResponse debit(
            @RequestBody DebitTransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        AuthUser user = getAuthUser();

        // Verify customer owns this account (optional - can be done in service layer)
        // For now, we'll let service handle it

        return transactionService.debit(request, idempotencyKey);
    }

    @PostMapping("/credit")
    public CreditTransactionResponse credit(
            @RequestBody CreditTransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        AuthUser user = getAuthUser();
        return transactionService.credit(request, idempotencyKey);
    }

    @PostMapping("/transfer")
    public TransferTransactionResponse transfer(
            @RequestBody TransferTransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        AuthUser user = getAuthUser();

        // Customer can only transfer from their own accounts
        // This validation should happen in service layer with account ownership check

        return transactionService.transfer(request, idempotencyKey);
    }

    private AuthUser getAuthUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();

        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }

        throw TransactionException.unauthorized("User not authenticated");
    }
}