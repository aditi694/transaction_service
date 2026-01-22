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
    public DebitTransactionResponse debit(DebitTransactionRequest req) {

        // 🔥 Auto-generate idempotency key
        String idempotencyKey = generateIdempotencyKey("DEBIT", req.getAccountNumber());

        // Check idempotency
        var existing = transactionRepo.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate debit request detected: {}", idempotencyKey);
            return buildDebitResponse(existing.get());
        }

        AuthUser user = currentUser();

        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        // Verify account exists and belongs to customer
        verifyAccountOwnership(user.getCustomerId(), req.getAccountNumber());

        // Check transaction limits
        checkTransactionLimits(req.getAccountNumber(), req.getAmount());

        BigDecimal balance = fetchBalance(req.getAccountNumber());
        TransactionValidator.validateBalance(balance, req.getAmount());

        // Execute debit
        executeDebit(req.getAccountNumber(), req.getAmount());

        BigDecimal newBalance = balance.subtract(req.getAmount());

        Transaction tx = Transaction.builder()
//                .id(UUID.randomUUID())
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
        sendNotificationAsync(tx);

        return buildDebitResponse(tx);
    }

    /* ================= CREDIT ================= */

    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {

        String idempotencyKey = generateIdempotencyKey("CREDIT", req.getAccountNumber());

        var existing = transactionRepo.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate credit request detected: {}", idempotencyKey);
            return buildCreditResponse(existing.get());
        }

        AuthUser user = currentUser();

        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        verifyAccountOwnership(user.getCustomerId(), req.getAccountNumber());

        BigDecimal balance = fetchBalance(req.getAccountNumber());
        executeCredit(req.getAccountNumber(), req.getAmount());

        Transaction tx = Transaction.builder()
//                .id(UUID.randomUUID())
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
        sendNotificationAsync(tx);

        return buildCreditResponse(tx);
    }

    /* ================= TRANSFER ================= */

    @Override
    public TransferTransactionResponse transfer(TransferTransactionRequest req) {

        String idempotencyKey = generateIdempotencyKey("TRANSFER", req.getFromAccount() + "-" + req.getToAccount());

        var existing = transactionRepo.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate transfer request detected: {}", idempotencyKey);
            return buildTransferResponse(existing.get());
        }

        AuthUser user = currentUser();

        TransactionValidator.validateTransfer(req.getFromAccount(), req.getToAccount());
        TransactionValidator.validateAmount(req.getAmount());

        // 🔥 Verify sender account ownership
        verifyAccountOwnership(user.getCustomerId(), req.getFromAccount());

        // 🔥 Verify receiver account exists
        verifyAccountExists(req.getToAccount());

        // Calculate charges
        BigDecimal charges = calculateTransferCharges(
                TransferMode.valueOf(req.getTransferType()),
                req.getAmount()
        );
        BigDecimal totalAmount = req.getAmount().add(charges);

        checkTransactionLimits(req.getFromAccount(), totalAmount);

        BigDecimal senderBalance = fetchBalance(req.getFromAccount());
        TransactionValidator.validateBalance(senderBalance, totalAmount);

        // Execute transfer
        executeDebit(req.getFromAccount(), totalAmount);
        executeCredit(req.getToAccount(), req.getAmount());

        Transaction tx = Transaction.builder()
//                .id(UUID.randomUUID())
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
        sendNotificationAsync(tx);
        notificationService.sendTransactionAlert(tx.getTransactionId());

        return buildTransferResponse(tx);
    }

    /* ================= ACCOUNT CLIENT OPERATIONS ================= */

    private BigDecimal fetchBalance(String accountNumber) {
        try {
            return accountClient.getBalance(accountNumber);
        } catch (FeignException e) {
            log.error("Failed to fetch balance: {}", e.getMessage());
            throw TransactionException.externalServiceError("Account service unavailable");
        }
    }

    private void executeDebit(String accountNumber, BigDecimal amount) {
        try {
            accountClient.debit(accountNumber, amount);
        } catch (FeignException e) {
            log.error("Failed to debit account: {}", e.getMessage());
            throw TransactionException.externalServiceError("Debit operation failed");
        }
    }

    private void executeCredit(String accountNumber, BigDecimal amount) {
        try {
            accountClient.credit(accountNumber, amount);
        } catch (FeignException e) {
            log.error("Failed to credit account: {}", e.getMessage());
            throw TransactionException.externalServiceError("Credit operation failed");
        }
    }

    private void verifyAccountExists(String accountNumber) {
        try {
            accountClient.getBalance(accountNumber);
        } catch (FeignException.NotFound e) {
            throw TransactionException.accountNotFound();
        } catch (FeignException e) {
            throw TransactionException.externalServiceError("Unable to verify account");
        }
    }

    private void verifyAccountOwnership(UUID customerId, String accountNumber) {
        // TODO: Call AccountClient to verify ownership
        // For now, assuming validation is done
        // In production: accountClient.verifyOwnership(customerId, accountNumber)
    }

    /* ================= LIMIT CHECKS ================= */

    private void checkTransactionLimits(String accountNumber, BigDecimal amount) {
        TransactionLimit limit = limitRepo
                .findById(accountNumber)
                .orElse(new TransactionLimit(accountNumber));

        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw TransactionException.limitExceeded(
                    "Per transaction limit of ₹" + limit.getPerTransactionLimit() + " exceeded"
            );
        }

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
                    "Daily limit of ₹" + limit.getDailyLimit() + " exceeded. Used: ₹" + todayTotal
            );
        }
    }

    /* ================= RESPONSE BUILDERS ================= */

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
                .referenceNumber(tx.getTransactionId())
                .build();
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
                .referenceNumber(tx.getTransactionId())
                .build();
    }

    private TransferTransactionResponse buildTransferResponse(Transaction tx) {
        return TransferTransactionResponse.builder()
                .success(true)
                .message("Transfer successful")
                .transactionId(tx.getTransactionId())
                .transferMode(tx.getTransferMode().name())
                .fromAccount(tx.getAccountNumber())
                .toAccount(tx.getToAccount())
                .amount(tx.getAmount())
                .charges(tx.getCharges())
                .totalDeducted(tx.getTotalAmount())
                .senderBalanceBefore(tx.getBalanceBefore())
                .senderBalanceAfter(tx.getBalanceAfter())
                .status(tx.getStatus().name())
                .timestamp(tx.getCreatedAt())
                .utrNumber(tx.getUtrNumber())
                .build();
    }

    /* ================= HELPERS ================= */

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

    private String generateIdempotencyKey(String type, String identifier) {
        return type + "-" + identifier + "-" + System.currentTimeMillis();
    }

    private AuthUser currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }
        throw TransactionException.unauthorized("User not authenticated");
    }

    private String generateTxnId() {
        return "TXN" + LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 17);
    }

    private String generateUTR() {
        return "UTR" + System.currentTimeMillis();
    }

    private void sendNotificationAsync(Transaction tx) {
        try {
            notificationService.sendTransactionAlert(String.valueOf(tx));
        } catch (Exception e) {
            log.error("Failed to send notification for txn: {}", tx.getTransactionId(), e);
        }
    }

}