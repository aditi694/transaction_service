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
import org.apache.commons.codec.digest.DigestUtils;
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

    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getAccountNumber());

        validateCategory(TransactionType.CREDIT, req.getCategory());

        String idempotencyKey = generateIdempotencyKey(
                req.getAccountNumber(),
                req.getAmount(),
                TransactionType.CREDIT,
                req.getCategory(),
                req.getDescription()
        );

        transactionRepo.findByIdempotencyKey(idempotencyKey)
                .ifPresent(tx -> {
                    throw TransactionException.badRequest(
                            "Duplicate request. Transaction already exists: " + tx.getTransactionId()
                    );
                });

        BigDecimal previousBalance =
                accountClient.getBalance(req.getAccountNumber());

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.CREDIT,
                req.getAmount(),
                BigDecimal.ZERO
        );

        tx.setCategory(req.getCategory());              // ✅ FIX
        tx.setDescription(req.getDescription());
        tx.setPreviousBalance(previousBalance);
        tx.setIdempotencyKey(idempotencyKey);            // ✅ FIX

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(tx);
        sagaService.processCredit(tx, saga);

        return CreditTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .status(TransactionStatus.IN_PROGRESS.name())
                .message("Transaction initiated")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getAccountNumber());

        validateCategory(TransactionType.DEBIT, req.getCategory());

        String idempotencyKey = generateIdempotencyKey(
                req.getAccountNumber(),
                req.getAmount(),
                TransactionType.DEBIT,
                req.getCategory(),
                req.getDescription()
        );

        transactionRepo.findByIdempotencyKey(idempotencyKey)
                .ifPresent(tx -> {
                    throw TransactionException.badRequest(
                            "Duplicate request. Transaction already exists: " + tx.getTransactionId()
                    );
                });

        checkTransactionLimits(req.getAccountNumber(), req.getAmount());

        BigDecimal previousBalance =
                accountClient.getBalance(req.getAccountNumber());

        Transaction tx = createTxn(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.DEBIT,
                req.getAmount(),
                BigDecimal.ZERO
        );

        tx.setCategory(req.getCategory());               // ✅ FIX
        tx.setDescription(req.getDescription());
        tx.setPreviousBalance(previousBalance);
        tx.setIdempotencyKey(idempotencyKey);            // ✅ FIX

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(tx);
        sagaService.processDebit(tx, saga);

        return DebitTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .status(TransactionStatus.IN_PROGRESS.name())
                .message("Transaction initiated")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public TransferInitiatedResponse transfer(TransferTransactionRequest req) {

        AuthUser user = currentUser();
        verifyOwnership(user.getCustomerId(), req.getFromAccount());

        String idempotencyKey = generateIdempotencyKey(
                req.getFromAccount(),
                req.getAmount(),
                TransactionType.TRANSFER,
                TransactionCategory.TRANSFER,   // ✅ forced
                req.getDescription()
        );

        transactionRepo.findByIdempotencyKey(idempotencyKey)
                .ifPresent(tx -> {
                    throw TransactionException.badRequest(
                            "Duplicate request. Transaction already exists: " + tx.getTransactionId()
                    );
                });

        BigDecimal charges = calculateTransferCharges(
                TransferMode.valueOf(req.getTransferType()),
                req.getAmount()
        );

        BigDecimal totalDebit = req.getAmount().add(charges);
        checkTransactionLimits(req.getFromAccount(), totalDebit);

        BigDecimal previousBalance =
                accountClient.getBalance(req.getFromAccount());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .toAccount(req.getToAccount())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.TRANSFER)
                .category(TransactionCategory.TRANSFER)   // ✅ FIX
                .amount(req.getAmount())
                .charges(charges)
                .totalAmount(totalDebit)
                .status(TransactionStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .previousBalance(previousBalance)
                .idempotencyKey(idempotencyKey)            // ✅ FIX
                .description(req.getDescription())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(tx);
        sagaService.processTransfer(tx, saga);

        return TransferInitiatedResponse.builder()
                .success(true)
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

        return TransactionStatusResponse.builder()
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .amount(tx.getAmount())
                .previousBalance(tx.getPreviousBalance())
                .currentBalance(tx.getCurrentBalance())
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
    private String generateIdempotencyKey(
            String account,
            BigDecimal amount,
            TransactionType type,
            TransactionCategory category,
            String description
    ) {
        String raw = account + "|" + amount + "|" + type + "|" + category + "|" + description;
        return DigestUtils.sha256Hex(raw);
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
    private void validateCategory(TransactionType type, TransactionCategory category) {
        if (category == null) {
            throw TransactionException.badRequest("Transaction category is required");
        }

        switch (type) {
            case CREDIT -> {
                if (!(category == TransactionCategory.SALARY ||
                        category == TransactionCategory.ATM ||
                        category == TransactionCategory.OTHERS)) {
                    throw TransactionException.badRequest("Invalid category for CREDIT");
                }
            }
            case DEBIT -> {
                if (!(category == TransactionCategory.FOOD ||
                        category == TransactionCategory.SHOPPING ||
                        category == TransactionCategory.BILL)) {
                    throw TransactionException.badRequest("Invalid category for DEBIT");
                }
            }
            case TRANSFER -> {
                if (category != TransactionCategory.TRANSFER) {
                    throw TransactionException.badRequest("TRANSFER must use TRANSFER category");
                }
            }
        }
    }
}