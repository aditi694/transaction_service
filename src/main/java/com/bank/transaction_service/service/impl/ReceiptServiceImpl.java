package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final TransactionRepository transactionRepository;

    @Override
    public byte[] generateReceipt(String transactionId) {

        Transaction tx = transactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(TransactionException::transactionNotFound);

        String receipt = """
                BANK TRANSACTION RECEIPT
                -------------------------
                Transaction ID : %s
                Type           : %s
                Amount         : ₹%s
                Charges        : ₹%s
                Total          : ₹%s
                Balance After  : ₹%s
                Date           : %s
                Status         : %s
                """.formatted(
                tx.getTransactionId(),
                tx.getTransactionType(),
                tx.getAmount(),
                tx.getCharges(),
                tx.getTotalAmount(),
                tx.getBalanceAfter(),
                tx.getCreatedAt(),
                tx.getStatus()
        );

        return receipt.getBytes(StandardCharsets.UTF_8);
    }
}
