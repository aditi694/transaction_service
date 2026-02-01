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
import feign.FeignException;
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

    /* =========================
       CREDIT
     ========================= */
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
        sagaService.credit(saga);

        return buildAsyncResponse(tx.getTransactionId(), TransactionStatus.IN_PROGRESS);
    }

    /* =========================
       DEBIT
     ========================= */
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
        sagaService.debit(saga);

        return buildAsyncDebitResponse(tx.getTransactionId(), TransactionStatus.IN_PROGRESS);
    }

    /* =========================
       TRANSFER
     ========================= */
    @Override
    public TransferTransactionResponse transfer(TransferTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getFromAccount());

        TransferMode mode = TransferMode.valueOf(req.getTransferType());
        BigDecimal charges = calculateTransferCharges(mode, req.getAmount());
        BigDecimal total = req.getAmount().add(charges);

        checkTransactionLimits(req.getFromAccount(), total);

        String idempotencyKey = generateIdempotencyKey(req);
        Transaction existing = findIdempotentTxn(idempotencyKey);

        if (existing != null) {
            return buildAsyncTransferResponse(existing.getTransactionId(), existing.getStatus());
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
                .totalAmount(total)
                .status(TransactionStatus.IN_PROGRESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(tx);
        sagaService.debit(saga);
        sagaService.credit(saga);

        return buildAsyncTransferResponse(tx.getTransactionId(), TransactionStatus.IN_PROGRESS);
    }

    /* =========================
       HELPERS
     ========================= */

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
        TransactionLimit limit = limitRepo
                .findById(accountNumber)
                .orElse(new TransactionLimit(accountNumber));

        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw TransactionException.limitExceeded("Per transaction limit exceeded");
        }
    }

    /* =========================
       RESPONSE BUILDERS (ASYNC)
     ========================= */

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

    private TransferTransactionResponse buildAsyncTransferResponse(String txnId, TransactionStatus status) {
        return TransferTransactionResponse.builder()
                .success(true)
                .transactionId(txnId)
                .status(status.name())
                .message("Transfer initiated")
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