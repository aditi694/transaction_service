package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/transactions")
@RequiredArgsConstructor
public class TransactionInternalController {

    private final TransactionRepository repo;

    @GetMapping("/total-debit")
    public ResponseEntity<BaseResponse<Double>> totalDebit(
            @RequestParam UUID customerId
    ) {
        double totalDebit =
                repo.findByCustomerId(customerId)
                        .stream()
                        .filter(t ->
                                t.getTransactionType() == TransactionType.DEBIT ||
                                        t.getTransactionType() == TransactionType.TRANSFER
                        )
                        .mapToDouble(t -> t.getAmount().doubleValue())
                        .sum();

        return ResponseEntity.ok(
                BaseResponse.success(
                        totalDebit,
                        "Total debit calculated successfully"
                )
        );
    }
}
