package com.bank.transaction_service.kafka.event;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionStatusListener {

    private final TransactionRepository repo;
    private final AccountClient accountClient;

    @KafkaListener(topics = "transaction-status", groupId = "transaction-service")
    public void handle(TransactionStatusEvent event) {

        Transaction tx = repo.findByTransactionId(event.transactionId())
                .orElseThrow();

        tx.setStatus(TransactionStatus.valueOf(event.finalStatus()));
        tx.setFailureReason(event.failureReason());
        tx.setCompletedAt(event.completedAt());
        if (tx.getStatus() == TransactionStatus.SUCCESS) {
            BigDecimal currentBalance =
                    accountClient.getBalance(tx.getAccountNumber());
            tx.setCurrentBalance(currentBalance);
        }
        repo.save(tx);

        log.info("Transaction {} moved to {}",
                tx.getTransactionId(), tx.getStatus());
    }
}