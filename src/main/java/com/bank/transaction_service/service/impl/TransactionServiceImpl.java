package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.*;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.kafka.producer.TransactionStatusProducer;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final TransactionLimitRepository limitRepo;
    private final AccountClient accountClient;
    private final TransactionSagaService sagaService;
    private final TransactionStatusProducer statusProducer; // ✅ ONLY kafka

    // ---------------- CREDIT ----------------
    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getAccountNumber());

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.CREDIT,
                req.getAmount(),
                BigDecimal.ZERO
        );

        TransactionSaga saga = sagaService.start(tx);

        try {
            accountClient.credit(req.getAccountNumber(), req.getAmount());

            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(tx);

            sagaService.markCompleted(saga);
            statusProducer.publish(tx);

        } catch (Exception ex) {

            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason(ex.getMessage());
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(tx);

            sagaService.markFailed(saga, ex.getMessage());
            statusProducer.publish(tx);
        }

        return buildAsyncResponse(tx);
    }

    // ---------------- DEBIT ----------------
    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getAccountNumber());
        checkTransactionLimits(req.getAccountNumber(), req.getAmount());

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.DEBIT,
                req.getAmount(),
                BigDecimal.ZERO
        );

        TransactionSaga saga = sagaService.start(tx);

        try {
            accountClient.debit(req.getAccountNumber(), req.getAmount());

            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(tx);

            sagaService.markCompleted(saga);
            statusProducer.publish(tx);

        } catch (Exception ex) {

            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason(ex.getMessage());
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(tx);

            sagaService.markFailed(saga, ex.getMessage());
            statusProducer.publish(tx);
        }

        return buildAsyncDebitResponse(tx);
    }

    // ---------------- TRANSFER ----------------
    @Override
    public TransferInitiatedResponse transfer(TransferTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getFromAccount());

        BigDecimal charges = calculateTransferCharges(
                TransferMode.valueOf(req.getTransferType()),
                req.getAmount()
        );

        BigDecimal totalDebit = req.getAmount().add(charges);
        checkTransactionLimits(req.getFromAccount(), totalDebit);

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .toAccount(req.getToAccount())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .charges(charges)
                .totalAmount(totalDebit)
                .status(TransactionStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);
        TransactionSaga saga = sagaService.start(tx);

        try {
            accountClient.transfer(
                    req.getFromAccount(),
                    req.getToAccount(),
                    req.getAmount(),
                    charges
            );

            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(tx);

            sagaService.markCompleted(saga);
            statusProducer.publish(tx);

        } catch (Exception ex) {

            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason(ex.getMessage());
            tx.setCompletedAt(LocalDateTime.now());
            transactionRepo.save(tx);

            sagaService.markFailed(saga, ex.getMessage());
            statusProducer.publish(tx);
        }

        return TransferInitiatedResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ---------------- GET STATUS ----------------
    @Override
    @Transactional(readOnly = true)
    public TransactionStatusResponse getStatus(String transactionId) {

        Transaction tx = transactionRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> TransactionException.notFound("Transaction not found"));

        return TransactionStatusResponse.builder()
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .amount(tx.getAmount())
                .failureReason(tx.getFailureReason())
                .createdAt(tx.getCreatedAt())
                .completedAt(tx.getCompletedAt())
                .message(tx.getStatus() == TransactionStatus.SUCCESS
                        ? "Transaction completed successfully"
                        : "Transaction failed")
                .build();
    }

    // ---------------- HELPERS ----------------

    private Transaction createTxn(String account, UUID customerId,
                                  TransactionType type,
                                  BigDecimal amount,
                                  BigDecimal charges) {

        return transactionRepo.save(
                Transaction.builder()
                        .transactionId(generateTxnId())
                        .accountNumber(account)
                        .customerId(customerId)
                        .transactionType(type)
                        .amount(amount)
                        .charges(charges)
                        .totalAmount(amount.add(charges))
                        .status(TransactionStatus.IN_PROGRESS)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private CreditTransactionResponse buildAsyncResponse(Transaction tx) {
        return CreditTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .message("Transaction processed")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private DebitTransactionResponse buildAsyncDebitResponse(Transaction tx) {
        return DebitTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .message("Transaction processed")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private AuthUser currentUser() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof AuthUser u) return u;
        throw TransactionException.unauthorized("Unauthenticated");
    }

    private String generateTxnId() {
        return "TXN-" + System.currentTimeMillis();
    }

    private void verifyOwnership(UUID customerId, String accountNumber) {
        UUID owner = accountClient.getAccountOwner(accountNumber);
        if (!owner.equals(customerId)) {
            throw TransactionException.unauthorized("Not your account");
        }
    }

    private void checkTransactionLimits(String accountNumber, BigDecimal amount) {
        TransactionLimit limit = limitRepo.findById(accountNumber)
                .orElse(new TransactionLimit(accountNumber));
        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw TransactionException.limitExceeded("Limit exceeded");
        }
    }

    private BigDecimal calculateTransferCharges(TransferMode mode, BigDecimal amount) {
        return switch (mode) {
            case IMPS -> BigDecimal.valueOf(5);
            case NEFT, UPI -> BigDecimal.ZERO;
            case RTGS -> amount.compareTo(BigDecimal.valueOf(200000)) > 0
                    ? BigDecimal.valueOf(30)
                    : BigDecimal.valueOf(25);
        };
    }
}