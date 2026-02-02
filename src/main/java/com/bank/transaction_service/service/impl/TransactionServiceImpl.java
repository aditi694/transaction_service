package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.*;
import com.bank.transaction_service.exception.TransactionException;
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


    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {
        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getAccountNumber());

        String idempotencyKey = generateIdempotencyKey(req);
        Transaction existing = findIdempotentTxn(idempotencyKey);

        if (existing != null) {
            return buildAsyncResponse(existing.getTransactionId(), existing.getStatus());
        }

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.CREDIT,
                req.getAmount(),
                BigDecimal.ZERO,
                idempotencyKey
        );

        TransactionSaga saga = sagaService.start(tx);
        sagaService.sendCredit(saga, req.getAmount());

        return buildAsyncResponse(tx.getTransactionId(), TransactionStatus.IN_PROGRESS);
    }

    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req) {
        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getAccountNumber());
        checkTransactionLimits(req.getAccountNumber(), req.getAmount());

        String idempotencyKey = generateIdempotencyKey(req);
        Transaction existing = findIdempotentTxn(idempotencyKey);

        if (existing != null) {
            return buildAsyncDebitResponse(existing.getTransactionId(), existing.getStatus());
        }

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.DEBIT,
                req.getAmount(),
                BigDecimal.ZERO,
                idempotencyKey
        );

        TransactionSaga saga = sagaService.start(tx);
        sagaService.sendDebit(saga, req.getAmount());

        return buildAsyncDebitResponse(tx.getTransactionId(), TransactionStatus.IN_PROGRESS);
    }

    @Override
    public TransferInitiatedResponse transfer(TransferTransactionRequest req) {
        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getFromAccount());

        TransferMode mode = TransferMode.valueOf(req.getTransferType());
        BigDecimal charges = calculateTransferCharges(mode, req.getAmount());
        BigDecimal totalDebit = req.getAmount().add(charges);

        checkTransactionLimits(req.getFromAccount(), totalDebit);

        String idempotencyKey = generateIdempotencyKey(req);
        Transaction existing = findIdempotentTxn(idempotencyKey);
        if (existing != null) {
            return TransferInitiatedResponse.builder()
                    .success(true)
                    .message("Duplicate transfer request")
                    .transactionId(existing.getTransactionId())
                    .status(existing.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .toAccount(req.getToAccount())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.TRANSFER)
                .transferMode(mode)
                .amount(req.getAmount())
                .charges(charges)
                .totalAmount(totalDebit)
                .status(TransactionStatus.IN_PROGRESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(tx);
        sagaService.sendDebit(saga, totalDebit);
        sagaService.sendCredit(saga, req.getAmount());

        return TransferInitiatedResponse.builder()
                .success(true)
                .message("Transfer initiated – poll status for updates")
                .transactionId(tx.getTransactionId())
                .status(TransactionStatus.IN_PROGRESS.name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionStatusResponse getStatus(String transactionId) {
        Transaction tx = transactionRepo.findByTransactionId(transactionId)
                .orElseThrow(() -> TransactionException.notFound("Transaction not found"));

        // Fetch live current balance
        BigDecimal currentBal = accountClient.getBalance(tx.getAccountNumber());

        return TransactionStatusResponse.builder()
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .amount(tx.getAmount())
                .previousBalance(tx.getPreviousBalance())
                .currentBalance(currentBal)
                .failureReason(tx.getFailureReason())
                .message(tx.getStatus() == TransactionStatus.SUCCESS ? "Transaction completed successfully" : null)
                .createdAt(tx.getCreatedAt())
                .completedAt(tx.getCompletedAt())
                .build();
    }


    private Transaction createTxn(
            String account,
            UUID customerId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal charges,
            String idempotencyKey
    ) {
        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(account)
                .customerId(customerId)
                .transactionType(type)
                .amount(amount)
                .charges(charges)
                .totalAmount(amount.add(charges))
                .status(TransactionStatus.IN_PROGRESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepo.save(tx);
    }

    private Transaction findIdempotentTxn(String key) {
        return transactionRepo.findByIdempotencyKey(key).orElse(null);
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
            throw TransactionException.limitExceeded("Per transaction limit exceeded");
        }
    }

    private CreditTransactionResponse buildAsyncResponse(String txnId, TransactionStatus status) {
        return CreditTransactionResponse.builder()
                .success(true)
                .transactionId(txnId)
                .status(status.name())
                .message("Transaction initiated")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private DebitTransactionResponse buildAsyncDebitResponse(String txnId, TransactionStatus status) {
        return DebitTransactionResponse.builder()
                .success(true)
                .transactionId(txnId)
                .status(status.name())
                .message("Transaction initiated")
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

    private String generateIdempotencyKey(Object req) {
        return req.hashCode() + "-" + LocalDate.now();
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