package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.request.BalanceUpdateRequest;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.SagaStatus;
import com.bank.transaction_service.enums.SagaStep;
import com.bank.transaction_service.kafka.producer.TransactionCommandProducer;
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
    private final TransactionCommandProducer commandProducer;

    public TransactionSaga start(Transaction tx) {
        return sagaRepo.save(
                TransactionSaga.builder()
                        .sagaId("SAGA-" + tx.getTransactionId())
                        .transactionId(tx.getTransactionId())
                        .fromAccount(tx.getAccountNumber())
                        .toAccount(tx.getToAccount())
                        .amount(tx.getTotalAmount())
                        .status(SagaStatus.IN_PROGRESS)
                        .currentStep(SagaStep.STARTED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    public void debit(TransactionSaga saga) {
        commandProducer.send(new com.bank.transaction_service.kafka.event.TransactionCommandEvent(
                1,
                UUID.randomUUID().toString(),
                saga.getTransactionId(),
                "DEBIT",
                saga.getFromAccount(),
                null,
                saga.getAmount()
        ));
        updateStep(saga, SagaStep.DEBIT_SENT);
    }

    public void credit(TransactionSaga saga) {
        commandProducer.send(new com.bank.transaction_service.kafka.event.TransactionCommandEvent(
                1,
                UUID.randomUUID().toString(),
                saga.getTransactionId(),
                "CREDIT",
                null,
                saga.getToAccount(),
                saga.getAmount()
        ));
        updateStep(saga, SagaStep.CREDIT_SENT);
    }

    public void compensate(TransactionSaga saga) {
        commandProducer.send(new com.bank.transaction_service.kafka.event.TransactionCommandEvent(
                1,
                UUID.randomUUID().toString(),
                saga.getTransactionId(),
                "COMPENSATE",
                saga.getFromAccount(),
                null,
                saga.getAmount()
        ));
        updateStep(saga, SagaStep.COMPENSATED);
    }

    private void updateStep(TransactionSaga saga, SagaStep step) {
        saga.setCurrentStep(step);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }
}