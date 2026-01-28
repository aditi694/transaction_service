package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
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

    public TransactionSaga start(
            String transactionId,
            BigDecimal amount,
            String fromAccount,
            String toAccount
    ) {

        TransactionSaga saga = TransactionSaga.builder()
                .sagaId("SAGA-" + transactionId)
                .transactionId(transactionId)
                .amount(amount)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .currentStep(SagaStep.STARTED)
                .status(SagaStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return sagaRepo.save(saga);
    }


    public void debit(TransactionSaga saga, String account, BigDecimal amount) {
        try {
            accountClient.debit(account, amount);
            updateStep(saga, SagaStep.DEBIT_DONE);
        } catch (Exception e) {
            failSaga(saga, "Debit failed");
            throw e;
        }
    }
    public void credit(TransactionSaga saga, String account, BigDecimal amount) {
        try {
            accountClient.credit(account, amount);
            updateStep(saga, SagaStep.CREDIT_DONE);
        } catch (Exception e) {
            compensateDebit(saga, account, amount);
            failSaga(saga, "Credit failed");
            throw e;
        }
    }

    public void complete(TransactionSaga saga) {
        saga.setCurrentStep(SagaStep.COMPLETED);
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }

    private void failSaga(TransactionSaga saga, String reason) {
        saga.setStatus(SagaStatus.FAILED);
        saga.setFailureReason(reason);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }

    private void compensateDebit(TransactionSaga saga, String account, BigDecimal amount) {
        try {
            accountClient.credit(account, amount);
            log.warn("Compensation successful for saga {}", saga.getSagaId());
        } catch (Exception ex) {
            log.error("CRITICAL: Compensation failed for saga {}", saga.getSagaId());
        }
    }

    private void updateStep(TransactionSaga saga, SagaStep step) {
        saga.setCurrentStep(step);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }
}
