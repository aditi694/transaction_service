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
        if (existing != null && existing.getStatus() == TransactionStatus.SUCCESS) {
            return buildCreditResponse(existing);
        }
        BigDecimal before = fetchBalance(req.getAccountNumber());

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.CREDIT,
                req.getAmount(),
                BigDecimal.ZERO,
                before,
                idempotencyKey
        );

        TransactionSaga saga = sagaService.start(tx);

        sagaService.credit(saga, req.getAccountNumber(), req.getAmount());
        sagaService.complete(saga);

        finalizeTxn(tx, fetchBalance(req.getAccountNumber()));

        return buildCreditResponse(tx);
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
        if (existing != null && existing.getStatus() == TransactionStatus.SUCCESS) {
            return buildDebitResponse(existing);
        }
        BigDecimal before = fetchBalance(req.getAccountNumber());

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.DEBIT,
                req.getAmount(),
                BigDecimal.ZERO,
                before,
                idempotencyKey
        );

        TransactionSaga saga = sagaService.start(tx);
        sagaService.debit(saga, req.getAccountNumber(), req.getAmount());
        sagaService.complete(saga);

        finalizeTxn(tx, fetchBalance(req.getAccountNumber()));

        return buildDebitResponse(tx);
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
        BigDecimal totalDeducted = req.getAmount().add(charges);

        checkTransactionLimits(req.getFromAccount(), totalDeducted);

        String idempotencyKey = generateIdempotencyKey(req);
        Transaction existing = findIdempotentTxn(idempotencyKey);
        if (existing != null && existing.getStatus() == TransactionStatus.SUCCESS) {
            return buildTransferResponse(existing);
        }
        BigDecimal before = fetchBalance(req.getFromAccount());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .toAccount(req.getToAccount())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.TRANSFER)
                .transferMode(mode)
                .amount(req.getAmount())
                .charges(charges)
                .totalAmount(totalDeducted)
                .balanceBefore(before)
                .status(TransactionStatus.IN_PROGRESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(tx);
        sagaService.debit(saga, req.getFromAccount(), totalDeducted);
        sagaService.credit(saga, req.getToAccount(), req.getAmount());
        sagaService.complete(saga);

        tx.setUtrNumber(generateUTR());
        finalizeTxn(tx, fetchBalance(req.getFromAccount()));

        return buildTransferResponse(tx);
    }

    /* =========================
       INTERNAL HELPERS
     ========================= */

    private void verifyOwnership(UUID customerId, String accountNumber) {
        UUID owner = accountClient.getAccountOwner(accountNumber);
        if (!owner.equals(customerId)) {
            throw TransactionException.unauthorized(
                    "You are not authorized to operate this account"
            );
        }
    }
    private void checkTransactionLimits(String accountNumber, BigDecimal amount) {

        TransactionLimit limit = limitRepo
                .findById(accountNumber)
                .orElse(new TransactionLimit(accountNumber));

        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw TransactionException.limitExceeded("Per transaction limit exceeded");
        }

        LocalDate today = LocalDate.now();

        BigDecimal todayTotal = transactionRepo
                .findByAccountNumberAndDate(accountNumber, today)
                .stream()
                .filter(t -> t.getTransactionType() != TransactionType.CREDIT)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTotal.add(amount).compareTo(limit.getDailyLimit()) > 0) {
            throw TransactionException.limitExceeded("Daily limit exceeded");
        }
    }
    private BigDecimal calculateTransferCharges(TransferMode mode, BigDecimal amount) {
        return switch (mode) {
            case IMPS -> BigDecimal.valueOf(5);
            case NEFT -> BigDecimal.ZERO;
            case RTGS -> amount.compareTo(BigDecimal.valueOf(200000)) > 0
                    ? BigDecimal.valueOf(30)
                    : BigDecimal.valueOf(25);
            case UPI -> BigDecimal.ZERO;
        };
    }
    private CreditTransactionResponse buildCreditResponse(Transaction tx) {
        return CreditTransactionResponse.builder()
                .success(true)
                .message("Amount credited successfully")
                .transactionId(tx.getTransactionId())
                .amount(tx.getAmount())
                .previousBalance(tx.getBalanceBefore())
                .currentBalance(tx.getBalanceAfter())
                .status(tx.getStatus().name())
                .timestamp(tx.getCreatedAt())
                .build();
    }

    private DebitTransactionResponse buildDebitResponse(Transaction tx) {
        return DebitTransactionResponse.builder()
                .success(true)
                .message("Transaction successful")
                .transactionId(tx.getTransactionId())
                .amount(tx.getAmount())
                .charges(tx.getCharges())
                .totalDeducted(tx.getTotalAmount())
                .previousBalance(tx.getBalanceBefore())
                .currentBalance(tx.getBalanceAfter())
                .status(tx.getStatus().name())
                .timestamp(tx.getCreatedAt())
                .build();
    }

    private TransferTransactionResponse buildTransferResponse(Transaction tx) {
        return TransferTransactionResponse.builder()
                .success(true)
                .message("Transfer completed successfully")
                .transactionId(tx.getTransactionId())
                .fromAccount(tx.getAccountNumber())
                .toAccount(tx.getToAccount())
                .amount(tx.getAmount())
                .charges(tx.getCharges())
                .totalDeducted(tx.getTotalAmount())
                .senderBalanceBefore(tx.getBalanceBefore())
                .senderBalanceAfter(tx.getBalanceAfter())
                .status(tx.getStatus().name())
                .utrNumber(tx.getUtrNumber())
                .timestamp(tx.getCreatedAt())
                .build();
    }
    private Transaction findIdempotentTxn(String key) {
        return transactionRepo.findByIdempotencyKey(key).orElse(null);
    }

    private Transaction createTxn(
            String account,
            UUID customerId,
            TransactionType type,
            BigDecimal amount,
            BigDecimal charges,
            BigDecimal before,
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
                .balanceBefore(before)
                .status(TransactionStatus.IN_PROGRESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepo.save(tx);
    }

    private void finalizeTxn(Transaction tx, BigDecimal after) {
        tx.setBalanceAfter(after);
        tx.setStatus(TransactionStatus.SUCCESS);
        transactionRepo.save(tx);
    }

    private BigDecimal fetchBalance(String account) {
        return accountClient.getBalance(account);
    }

    private AuthUser currentUser() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (p instanceof AuthUser u) return u;
        throw TransactionException.unauthorized("Unauthenticated");
    }

    private String generateTxnId() {
        return "TXN-" + System.currentTimeMillis();
    }

    private String generateUTR() {
        return "UTR-" + System.currentTimeMillis();
    }

    private String generateIdempotencyKey(Object req) {
        return req.hashCode() + "-" + LocalDate.now();
    }

}