package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.SagaStatus;
import com.bank.transaction_service.enums.SagaStep;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.kafka.producer.TransactionStatusProducer;
import com.bank.transaction_service.repository.TransactionSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSagaService {

    private final TransactionSagaRepository sagaRepo;
    private final AccountClient accountClient;
    private final TransactionStatusProducer statusProducer;

    public TransactionSaga start(Transaction tx) {

        // ✅ DEFENSIVE, DB-SAFE AMOUNT
        BigDecimal sagaAmount =
                tx.getTotalAmount() != null
                        ? tx.getTotalAmount()
                        : (tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO);

        if (sagaAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Invalid saga amount for transaction " + tx.getTransactionId()
            );
        }

        return sagaRepo.save(
                TransactionSaga.builder()
                        .sagaId("SAGA-" + tx.getTransactionId())
                        .transactionId(tx.getTransactionId())
                        .fromAccount(tx.getAccountNumber())
                        .toAccount(tx.getToAccount())
                        .amount(sagaAmount)                 // ✅ NEVER NULL
                        .status(SagaStatus.IN_PROGRESS)
                        .currentStep(SagaStep.INITIATED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    public void processCredit(Transaction tx, TransactionSaga saga) {
        try {
            saga.setCurrentStep(SagaStep.CREDIT);
            sagaRepo.save(saga);

            accountClient.credit(tx.getAccountNumber(), tx.getAmount());

            sagaSuccess(tx, saga);

        } catch (Exception ex) {
            sagaFailure(tx, saga, ex);
        }
    }

    public void processDebit(Transaction tx, TransactionSaga saga) {
        try {
            saga.setCurrentStep(SagaStep.DEBIT);
            sagaRepo.save(saga);

            accountClient.debit(tx.getAccountNumber(), tx.getAmount());

            sagaSuccess(tx, saga);

        } catch (Exception ex) {
            sagaFailure(tx, saga, ex);
        }
    }

    public void processTransfer(Transaction tx, TransactionSaga saga) {
        try {
            saga.setCurrentStep(SagaStep.TRANSFER);
            sagaRepo.save(saga);

            accountClient.transfer(
                    tx.getAccountNumber(),
                    tx.getToAccount(),
                    tx.getAmount(),
                    tx.getCharges()
            );

            sagaSuccess(tx, saga);

        } catch (Exception ex) {
            sagaFailure(tx, saga, ex);
        }
    }

    private void sagaSuccess(Transaction tx, TransactionSaga saga) {

        BigDecimal currentBalance =
                tx.getTransactionType() == TransactionType.TRANSFER
                        ? accountClient.getBalance(tx.getAccountNumber())
                        : accountClient.getBalance(tx.getAccountNumber());

        tx.setCurrentBalance(currentBalance);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setCompletedAt(LocalDateTime.now());

        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCurrentStep(SagaStep.COMPLETED);
        saga.setUpdatedAt(LocalDateTime.now());

        sagaRepo.save(saga);

        statusProducer.publishSuccess(tx);
    }

    private void sagaFailure(Transaction tx, TransactionSaga saga, Exception ex) {
        saga.setStatus(SagaStatus.FAILED);
        saga.setCurrentStep(SagaStep.FAILED);
        saga.setFailureReason(ex.getMessage());
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);

        statusProducer.publishFailure(tx, ex.getMessage());
    }
}