package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.security.AuthUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transaction")
public class TransactionController {

    @PostMapping("/debit")
    public TransactionResponse debit(@RequestBody DebitTransactionRequest request) {
        AuthUser user = getUser();
        return null; // will call service later
    }

    @PostMapping("/credit")
    public TransactionResponse credit(@RequestBody CreditTransactionRequest request) {
        AuthUser user = getUser();
        return null;
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(@RequestBody TransferTransactionRequest request) {
        AuthUser user = getUser();
        return null;
    }

    private AuthUser getUser() {
        return (AuthUser) SecurityContextHolder
                .getContext()
                .getAuthentication();
    }
}
