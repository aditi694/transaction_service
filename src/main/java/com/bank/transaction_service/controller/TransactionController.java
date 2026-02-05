package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/debit")
    public ResponseEntity<BaseResponse<DebitTransactionResponse>> debit(
            @RequestBody DebitTransactionRequest request
    ) {
        DebitTransactionResponse response =
                transactionService.debit(request);

        return ResponseEntity.ok(
                BaseResponse.success(response, "Debit transaction initiated")
        );
    }

    @PostMapping("/credit")
    public ResponseEntity<BaseResponse<CreditTransactionResponse>> credit(
            @RequestBody CreditTransactionRequest request
    ) {
        CreditTransactionResponse response =
                transactionService.credit(request);

        return ResponseEntity.ok(
                BaseResponse.success(response, "Credit transaction initiated")
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<BaseResponse<TransferInitiatedResponse>> transfer(
            @RequestBody TransferTransactionRequest request
    ) {
        TransferInitiatedResponse response =
                transactionService.transfer(request);

        return ResponseEntity.ok(
                BaseResponse.success(response, "Transfer transaction initiated")
        );
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<BaseResponse<TransactionStatusResponse>> getStatus(
            @PathVariable String transactionId
    ) {
        TransactionStatusResponse response =
                transactionService.getStatus(transactionId);

        return ResponseEntity.ok(
                BaseResponse.success(response, "Transaction status fetched successfully")
        );
    }
}
