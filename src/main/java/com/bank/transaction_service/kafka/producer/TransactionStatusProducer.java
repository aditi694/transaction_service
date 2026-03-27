package com.bank.transaction_service.kafka.producer;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.kafka.event.TransactionStatusEvent;
import com.bank.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionStatusProducer {

    private final KafkaTemplate<String, TransactionStatusEvent> kafkaTemplate;
    private final TransactionRepository transactionRepo;

    public void publishSuccess(Transaction tx) {

        transactionRepo.save(tx);

        TransactionStatusEvent event =
                new TransactionStatusEvent(
                        tx.getTransactionId(),
                        tx.getTransactionType().name(),
                        tx.getAccountNumber(),
                        tx.getToAccount(),
                        tx.getAmount(),
                        TransactionStatus.SUCCESS.name(),
                        null,
                        tx.getCreatedAt(),
                        tx.getCompletedAt()
                );

        kafkaTemplate.send("transaction-status", tx.getTransactionId(), event);
    }

    public void publishFailure(Transaction tx, String reason) {

        transactionRepo.save(tx);

        TransactionStatusEvent event =
                new TransactionStatusEvent(
                        tx.getTransactionId(),
                        tx.getTransactionType().name(),
                        tx.getAccountNumber(),
                        tx.getToAccount(),
                        tx.getAmount(),
                        TransactionStatus.FAILED.name(),
                        reason,
                        tx.getCreatedAt(),
                        tx.getCompletedAt()
                );

        kafkaTemplate.send("transaction-status", tx.getTransactionId(), event);
    }
}
