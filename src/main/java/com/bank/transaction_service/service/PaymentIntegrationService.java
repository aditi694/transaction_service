package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.PaymentEventMessage;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentIntegrationService {

    private final TransactionRepository transactionRepository;

    public PaymentIntegrationService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void handlePaymentSuccess(PaymentEventMessage event) {

        if (!"SUCCESS".equals(event.getStatus())) {
            return;
        }
        Optional<Transaction> existing =
                transactionRepository.findByIdempotencyKey(event.getPaymentId());

        if (existing.isPresent()) {
            return;
        }

        Transaction tx = new Transaction();

        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setCustomerId(UUID.fromString(event.getUserId()));
        tx.setAccountNumber("TEMP_ACCOUNT");

        tx.setTransactionType(TransactionType.CREDIT);
        tx.setAmount(BigDecimal.valueOf(event.getAmount()));

        tx.setCharges(BigDecimal.ZERO);
        tx.setTotalAmount(BigDecimal.valueOf(event.getAmount()));

        tx.setStatus(TransactionStatus.SUCCESS);

        tx.setIdempotencyKey(event.getPaymentId());

        tx.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(tx);
    }
}