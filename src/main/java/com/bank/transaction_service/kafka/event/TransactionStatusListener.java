package com.bank.transaction_service.kafka.event;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionStatusListener {

    private final TransactionRepository repo;

    @KafkaListener(topics = "transaction-status", groupId = "transaction-service")
    public void handle(TransactionStatusEvent event) {

        Transaction tx = repo.findById(event.transactionId())
                .orElseThrow();

        tx.setStatus(TransactionStatus.valueOf(event.finalStatus()));
        tx.setFailureReason(event.failureReason());

        repo.save(tx);
    }
}