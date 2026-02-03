// src/main/java/com/bank/transaction_service/service/impl/TransactionSagaService.java
package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.SagaStatus;
import com.bank.transaction_service.enums.SagaStep;
import com.bank.transaction_service.repository.TransactionSagaRepository;
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

    public TransactionSaga start(Transaction tx) {

        TransactionSaga saga = TransactionSaga.builder()
                .sagaId("SAGA-" + tx.getTransactionId())
                .transactionId(tx.getTransactionId())
                .fromAccount(tx.getAccountNumber())
                .toAccount(tx.getToAccount())
                .amount(tx.getTotalAmount() != null ? tx.getTotalAmount() : tx.getAmount())
                .status(SagaStatus.IN_PROGRESS)
                .currentStep(SagaStep.INITIATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return sagaRepo.save(saga);
    }

    public void markCompleted(TransactionSaga saga) {
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCurrentStep(SagaStep.COMPLETED);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }

    public void markFailed(TransactionSaga saga, String reason) {
        saga.setStatus(SagaStatus.FAILED);
        saga.setFailureReason(reason);
        saga.setCurrentStep(SagaStep.FAILED);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }
}