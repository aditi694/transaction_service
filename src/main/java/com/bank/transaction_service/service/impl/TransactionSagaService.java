package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.request.BalanceUpdateRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionSagaService {

    private final TransactionSagaRepository sagaRepo;
    private final AccountClient accountClient;

    public TransactionSaga start(Transaction tx) {
        return sagaRepo.save(
                TransactionSaga.builder()
                        .sagaId("SAGA-" + tx.getTransactionId())
                        .transactionId(tx.getTransactionId())
                        .amount(tx.getAmount())
                        .fromAccount(tx.getAccountNumber())
                        .toAccount(tx.getToAccount())
                        .status(SagaStatus.IN_PROGRESS)
                        .currentStep(SagaStep.STARTED)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    public void debit(TransactionSaga saga, String account, BigDecimal amount) {
        accountClient.updateBalance(
                BalanceUpdateRequest.builder()
                        .accountNumber(account)
                        .delta(amount.negate())
                        .transactionId(saga.getTransactionId())
                        .build()
        );
        updateStep(saga, SagaStep.DEBIT_DONE);
    }

    public void credit(TransactionSaga saga, String account, BigDecimal amount) {
        try {
            accountClient.updateBalance(
                    BalanceUpdateRequest.builder()
                            .accountNumber(account)
                            .delta(amount)
                            .transactionId(saga.getTransactionId())
                            .build()
            );
            updateStep(saga, SagaStep.CREDIT_DONE);
        } catch (Exception e) {
            compensateDebit(saga);
            failSaga(saga, "Credit failed");
            throw e;
        }
    }

    public void complete(TransactionSaga saga) {
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCurrentStep(SagaStep.COMPLETED);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }

    private void compensateDebit(TransactionSaga saga) {
        accountClient.updateBalance(
                BalanceUpdateRequest.builder()
                        .accountNumber(saga.getFromAccount())
                        .delta(saga.getAmount())
                        .transactionId(saga.getTransactionId() + "-COMP")
                        .build()
        );
    }

    private void updateStep(TransactionSaga saga, SagaStep step) {
        saga.setCurrentStep(step);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }

    private void failSaga(TransactionSaga saga, String reason) {
        saga.setStatus(SagaStatus.FAILED);
        saga.setFailureReason(reason);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }
}