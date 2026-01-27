package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/debit")
    public DebitTransactionResponse debit(@RequestBody DebitTransactionRequest request) {
        return transactionService.debit(request);
    }

    @PostMapping("/credit")
    public CreditTransactionResponse credit(@RequestBody CreditTransactionRequest request) {
        return transactionService.credit(request);
    }


    @PostMapping("/transfer")
    public TransferTransactionResponse transfer(@RequestBody TransferTransactionRequest request) {
        return transactionService.transfer(request);
    }

    private AuthUser getAuthUser() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }

        throw TransactionException.unauthorized("User not authenticated");
    }

}