package com.bank.transaction_service.kafka.producer;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.kafka.event.TransactionStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionStatusProducer {

    private final KafkaTemplate<String, TransactionStatusEvent> kafkaTemplate;

    public void publish(Transaction tx) {

        TransactionStatusEvent event =
                new TransactionStatusEvent(
                        tx.getTransactionId(),
                        tx.getTransactionType().name(),
                        tx.getAccountNumber(),
                        tx.getToAccount(),
                        tx.getAmount(),
                        tx.getStatus().name(),
                        tx.getFailureReason(),
                        tx.getCreatedAt(),
                        tx.getCompletedAt()
                );

        kafkaTemplate.send("transaction-status", tx.getTransactionId(), event);
    }
}