package com.bank.transaction_service.controller;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/transactions")
@RequiredArgsConstructor
public class TransactionInternalController {

    private final TransactionRepository repo;

    @GetMapping("/total-debit")
    public double totalDebit(@RequestParam UUID customerId) {
        return repo.findByCustomerId(customerId)
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEBIT ||
                        t.getTransactionType() == TransactionType.TRANSFER)
                .map(t -> t.getAmount().doubleValue())
                .reduce(0.0, Double::sum);
    }
}