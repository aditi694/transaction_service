package com.bank.transaction_service.kafka.producer;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.kafka.event.TransactionStatusEvent;
import com.bank.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionStatusProducerTest {

    @Mock
    private KafkaTemplate<String, TransactionStatusEvent> kafkaTemplate;

    @Mock
    private TransactionRepository transactionRepo;

    @InjectMocks
    private TransactionStatusProducer producer;

    private Transaction createTransaction() {
        Transaction tx = new Transaction();
        tx.setTransactionId("TX123");
        tx.setTransactionType(TransactionType.DEBIT);
        tx.setAccountNumber("ACC1");
        tx.setToAccount("ACC2");
        tx.setAmount(BigDecimal.TEN);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setCompletedAt(LocalDateTime.now());
        tx.setStatus(TransactionStatus.PENDING);
        return tx;
    }

    @Test
    void publishSuccess_shouldSaveAndSendEvent() {
        Transaction tx = createTransaction();

        producer.publishSuccess(tx);

        verify(transactionRepo).save(tx);
        verify(kafkaTemplate)
                .send(eq("transaction-status"), eq("TX123"), any(TransactionStatusEvent.class));
    }

    @Test
    void publishFailure_shouldSaveAndSendEvent() {
        Transaction tx = createTransaction();

        producer.publishFailure(tx, "Insufficient balance");

        verify(transactionRepo).save(tx);
        verify(kafkaTemplate)
                .send(eq("transaction-status"), eq("TX123"), any(TransactionStatusEvent.class));
    }
}
