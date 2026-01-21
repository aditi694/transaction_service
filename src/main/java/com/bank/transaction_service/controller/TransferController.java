package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.TransferTransactionRequest;
import com.bank.transaction_service.dto.response.TransactionResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transaction")
public class TransferController {

    @PostMapping("/transfer")
    public TransactionResponse transfer(
            @RequestBody TransferTransactionRequest request
    ) {
        return null; // service later
    }
}
