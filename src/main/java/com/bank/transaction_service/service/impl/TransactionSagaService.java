// src/main/java/com/bank/transaction_service/service/impl/TransactionSagaService.java
package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.SagaStatus;
import com.bank.transaction_service.enums.SagaStep;
import com.bank.transaction_service.kafka.producer.TransactionCommandProducer;
import com.bank.transaction_service.repository.TransactionSagaRepository;
import com.bank.transaction_service.kafka.event.TransactionCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSagaService {

    private final TransactionSagaRepository sagaRepo;
    private final TransactionCommandProducer commandProducer;

    public TransactionSaga start(Transaction tx) {
        return sagaRepo.save(TransactionSaga.builder()
                .sagaId("SAGA-" + tx.getTransactionId())
                .transactionId(tx.getTransactionId())
                .fromAccount(tx.getAccountNumber())
                .toAccount(tx.getToAccount())
                .amount(tx.getTotalAmount() != null ? tx.getTotalAmount() : tx.getAmount())
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(SagaStep.INITIATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    public void sendDebit(TransactionSaga saga, BigDecimal amount) {
        commandProducer.send(new TransactionCommandEvent(
                1,
                UUID.randomUUID().toString(),
                saga.getTransactionId(),
                "DEBIT",
                saga.getFromAccount(),
                amount
        ));
        updateStep(saga, SagaStep.DEBIT_SENT);
    }

    public void sendCredit(TransactionSaga saga, BigDecimal amount) {
        commandProducer.send(new TransactionCommandEvent(
                1,
                UUID.randomUUID().toString(),
                saga.getTransactionId(),
                "CREDIT",
                saga.getToAccount(),
                amount
        ));
        updateStep(saga, SagaStep.CREDIT_SENT);
    }

    public void sendCompensation(TransactionSaga saga, BigDecimal amount) {
        commandProducer.send(new TransactionCommandEvent(
                1,
                UUID.randomUUID().toString(),
                saga.getTransactionId(),
                "COMPENSATE_DEBIT",
                saga.getFromAccount(),
                amount
        ));
    }

    private void updateStep(TransactionSaga saga, SagaStep step) {
        saga.setCurrentStep(step);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }
}