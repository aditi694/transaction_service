package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class TransactionQueryController {

    private final TransactionQueryService queryService;

    @GetMapping("/transactions")
    public TransactionHistoryResponse history(
            @RequestParam("account_number") String accountNumber,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "1") int page
    ) {
        return queryService.getHistory(accountNumber, limit, page);
    }

    @GetMapping("/mini-statement")
    public MiniStatementResponse miniStatement(
            @RequestParam("account_number") String accountNumber
    ) {
        return queryService.miniStatement(accountNumber);
    }
}
