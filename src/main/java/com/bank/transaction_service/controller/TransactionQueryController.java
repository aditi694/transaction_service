package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class TransactionQueryController {

    @GetMapping("/transactions")
    public TransactionHistoryResponse history(
            @RequestParam String account_number,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "1") int page
    ) {
        return null;
    }

    @GetMapping("/transaction/{transactionId}")
    public TransactionDetailResponse getOne(
            @PathVariable String transactionId
    ) {
        return null;
    }

    @GetMapping("/mini-statement")
    public MiniStatementResponse miniStatement(
            @RequestParam String account_number
    ) {
        return null;
    }
}
