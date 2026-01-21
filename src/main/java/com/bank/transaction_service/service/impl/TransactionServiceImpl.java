package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.enums.*;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.NotificationService;
import com.bank.transaction_service.service.TransactionService;
import com.bank.transaction_service.validation.TransactionValidator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final TransactionLimitRepository limitRepo;
    private final AccountClient accountClient;
    private final NotificationService notificationService;

    /* ================= DEBIT ================= */

    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req, String idempotencyKey) {

        // Check idempotency
        if (idempotencyKey != null) {
            Optional<Transaction> existing = transactionRepo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate request detected with key: {}", idempotencyKey);
                return DebitTransactionResponse.builder()
                        .success(true)
                        .transactionId(existing.get().getTransactionId())
                        .build();
            }
        }

        AuthUser user = currentUser();

        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        // Check transaction limits
        checkTransactionLimits(req.getAccountNumber(), req.getAmount());

        BigDecimal balance;
        try {
            balance = accountClient.getBalance(req.getAccountNumber());
        } catch (FeignException e) {
            log.error("Failed to fetch balance: {}", e.getMessage());
            throw TransactionException.externalServiceError("Account service unavailable");
        }

        TransactionValidator.validateBalance(balance, req.getAmount());

        try {
            accountClient.debit(req.getAccountNumber(), req.getAmount());
        } catch (FeignException e) {
            log.error("Failed to debit account: {}", e.getMessage());
            throw TransactionException.externalServiceError("Debit operation failed");
        }

        BigDecimal newBalance = balance.subtract(req.getAmount());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getAccountNumber())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.DEBIT)
                .category(req.getCategory())
                .amount(req.getAmount())
                .charges(BigDecimal.ZERO)
                .totalAmount(req.getAmount())
                .balanceBefore(balance)
                .balanceAfter(newBalance)
                .description(req.getDescription())
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        // Send notification asynchronously (won't rollback transaction if fails)
        try {
            notificationService.sendTransactionAlert(tx);
        } catch (Exception e) {
            log.error("Failed to send notification for txn: {}", tx.getTransactionId(), e);
        }

        return DebitTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .build();
    }

    /* ================= CREDIT ================= */

    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req, String idempotencyKey) {

        // Check idempotency
        if (idempotencyKey != null) {
            Optional<Transaction> existing = transactionRepo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate request detected with key: {}", idempotencyKey);
                return CreditTransactionResponse.builder()
                        .success(true)
                        .transactionId(existing.get().getTransactionId())
                        .build();
            }
        }

        AuthUser user = currentUser();

        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        BigDecimal balance;
        try {
            balance = accountClient.getBalance(req.getAccountNumber());
        } catch (FeignException e) {
            log.error("Failed to fetch balance: {}", e.getMessage());
            throw TransactionException.externalServiceError("Account service unavailable");
        }

        try {
            accountClient.credit(req.getAccountNumber(), req.getAmount());
        } catch (FeignException e) {
            log.error("Failed to credit account: {}", e.getMessage());
            throw TransactionException.externalServiceError("Credit operation failed");
        }

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getAccountNumber())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.CREDIT)
                .category(req.getCategory())
                .amount(req.getAmount())
                .charges(BigDecimal.ZERO)
                .totalAmount(req.getAmount())
                .balanceBefore(balance)
                .balanceAfter(balance.add(req.getAmount()))
                .description(req.getDescription())
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        try {
            notificationService.sendTransactionAlert(tx);
        } catch (Exception e) {
            log.error("Failed to send notification for txn: {}", tx.getTransactionId(), e);
        }

        return CreditTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .build();
    }

    /* ================= TRANSFER ================= */

    @Override
    public TransferTransactionResponse transfer(TransferTransactionRequest req, String idempotencyKey) {

        // Check idempotency
        if (idempotencyKey != null) {
            Optional<Transaction> existing = transactionRepo.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Duplicate request detected with key: {}", idempotencyKey);
                return TransferTransactionResponse.builder()
                        .success(true)
                        .transactionId(existing.get().getTransactionId())
                        .utrNumber(existing.get().getUtrNumber())
                        .build();
            }
        }

        AuthUser user = currentUser();

        TransactionValidator.validateTransfer(req.getFromAccount(), req.getToAccount());
        TransactionValidator.validateAmount(req.getAmount());

        // Calculate charges based on transfer mode
        BigDecimal charges = calculateTransferCharges(
                TransferMode.valueOf(req.getTransferType()),
                req.getAmount()
        );

        BigDecimal totalAmount = req.getAmount().add(charges);

        // Check limits with total amount (including charges)
        checkTransactionLimits(req.getFromAccount(), totalAmount);

        BigDecimal senderBalance;
        try {
            senderBalance = accountClient.getBalance(req.getFromAccount());
        } catch (FeignException e) {
            log.error("Failed to fetch sender balance: {}", e.getMessage());
            throw TransactionException.externalServiceError("Account service unavailable");
        }

        TransactionValidator.validateBalance(senderBalance, totalAmount);

        try {
            // Debit sender (amount + charges)
            accountClient.debit(req.getFromAccount(), totalAmount);

            // Credit receiver (only amount, not charges)
            accountClient.credit(req.getToAccount(), req.getAmount());
        } catch (FeignException e) {
            log.error("Failed to complete transfer: {}", e.getMessage());
            throw TransactionException.externalServiceError("Transfer operation failed");
        }

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .customerId(user.getCustomerId())
                .toAccount(req.getToAccount())
                .transactionType(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .charges(charges)
                .totalAmount(totalAmount)
                .balanceBefore(senderBalance)
                .balanceAfter(senderBalance.subtract(totalAmount))
                .transferMode(TransferMode.valueOf(req.getTransferType()))
                .description(req.getDescription())
                .utrNumber(generateUTR())
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        try {
            notificationService.sendTransactionAlert(tx);
        } catch (Exception e) {
            log.error("Failed to send notification for txn: {}", tx.getTransactionId(), e);
        }

        return TransferTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .utrNumber(tx.getUtrNumber())
                .build();
    }

    /* ================= HELPERS ================= */

    private void checkTransactionLimits(String accountNumber, BigDecimal amount) {
        TransactionLimit limit = limitRepo
                .findById(accountNumber)
                .orElse(new TransactionLimit(accountNumber));

        // Check per-transaction limit
        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw TransactionException.limitExceeded(
                    "Per transaction limit of ₹" + limit.getPerTransactionLimit() + " exceeded"
            );
        }

        // Check daily limit
        LocalDate today = LocalDate.now();
        BigDecimal todayTotal = transactionRepo
                .findByAccountNumberAndDate(accountNumber, today)
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEBIT
                        || t.getTransactionType() == TransactionType.TRANSFER)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTotal.add(amount).compareTo(limit.getDailyLimit()) > 0) {
            throw TransactionException.limitExceeded(
                    "Daily limit of ₹" + limit.getDailyLimit() + " exceeded"
            );
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

    private AuthUser currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }
        throw TransactionException.unauthorized("User not authenticated");
    }

    private String generateTxnId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12);
    }

    private String generateUTR() {
        return "UTR" + System.currentTimeMillis();
    }
}