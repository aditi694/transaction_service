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
import com.bank.transaction_service.service.NotificationService;
import com.bank.transaction_service.service.TransactionService;
import com.bank.transaction_service.validation.TransactionValidator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
    private final TransactionSagaService sagaService;


    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req) {

        AuthUser user = currentUser();
        validateDebit(req);
        verifyAccountOwnership(user.getCustomerId(), req.getAccountNumber());

        String idempotencyKey =
                generateIdempotencyKey("DEBIT", req.getAccountNumber(), user.getCustomerId());

        Transaction existing =
                transactionRepo.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) return buildDebitResponse(existing);

        BigDecimal balanceBefore = fetchBalance(req.getAccountNumber());

        Transaction tx = createPendingTransaction(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.DEBIT,
                req.getAmount(),
                idempotencyKey
        );

        try {
            executeDebit(req.getAccountNumber(), req.getAmount());

            tx.setBalanceBefore(balanceBefore);
            tx.setBalanceAfter(balanceBefore.subtract(req.getAmount()));
            tx.setCharges(BigDecimal.ZERO);
            tx.setTotalAmount(req.getAmount());
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setCreatedAt(LocalDateTime.now());

            transactionRepo.save(tx);
            notificationService.sendTransactionAlert(tx.getTransactionId());

            return buildDebitResponse(tx);

        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(tx);
            throw TransactionException.externalServiceError("Debit failed");
        }
    }


    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {

        AuthUser user = currentUser();
        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        String idempotencyKey =
                generateIdempotencyKey("CREDIT", req.getAccountNumber(), user.getCustomerId());

        Transaction existing =
                transactionRepo.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existing != null) return buildCreditResponse(existing);

        BigDecimal balanceBefore = fetchBalance(req.getAccountNumber());

        Transaction tx = createPendingTransaction(
                req.getAccountNumber(),
                user.getCustomerId(),
                TransactionType.CREDIT,
                req.getAmount(),
                idempotencyKey
        );

        try {
            executeCredit(req.getAccountNumber(), req.getAmount());

            tx.setBalanceBefore(balanceBefore);
            tx.setBalanceAfter(balanceBefore.add(req.getAmount()));
            tx.setTotalAmount(req.getAmount());
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setCreatedAt(LocalDateTime.now());

            transactionRepo.save(tx);
            notificationService.sendTransactionAlert(tx.getTransactionId());

            return buildCreditResponse(tx);

        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(tx);
            throw TransactionException.externalServiceError("Credit failed");
        }
    }
    @Override
    public TransferTransactionResponse transfer(TransferTransactionRequest req) {

        AuthUser user = currentUser();

        TransactionValidator.validateTransfer(req.getFromAccount(), req.getToAccount());
        TransactionValidator.validateAmount(req.getAmount());
        verifyAccountOwnership(user.getCustomerId(), req.getFromAccount());

        // ✅ ALWAYS PARSE MODE FIRST
        TransferMode mode = TransferMode.valueOf(req.getTransferType().toUpperCase());

        BigDecimal charges = calculateTransferCharges(mode, req.getAmount());
        BigDecimal totalAmount = req.getAmount().add(charges);

        checkTransactionLimits(req.getFromAccount(), totalAmount);

        String idempotencyKey = generateIdempotencyKey(
                "TRANSFER",
                req.getFromAccount() + req.getToAccount(),
                user.getCustomerId()
        );

        Transaction existing = transactionRepo.findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existing != null) {
            return buildTransferResponse(existing);
        }

        BigDecimal balanceBefore = fetchBalance(req.getFromAccount());

        Transaction tx = createPendingTransaction(
                req.getFromAccount(),
                user.getCustomerId(),
                TransactionType.TRANSFER,
                req.getAmount(),
                idempotencyKey
        );

        // ✅ SET NON-NEGOTIABLE FIELDS EARLY
        tx.setTransferMode(mode);
        tx.setCharges(charges);
        tx.setTotalAmount(totalAmount);

        transactionRepo.save(tx);

        TransactionSaga saga =
                sagaService.start("SAGA-" + tx.getTransactionId());

        try {
            sagaService.debit(saga, req.getFromAccount(), totalAmount);
            sagaService.credit(saga, req.getToAccount(), req.getAmount());

            tx.setToAccount(req.getToAccount());
            tx.setBalanceBefore(balanceBefore);
            tx.setBalanceAfter(balanceBefore.subtract(totalAmount));
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setUtrNumber(generateUTR());
            tx.setCreatedAt(LocalDateTime.now());

            transactionRepo.save(tx);
            sagaService.complete(saga);

            return buildTransferResponse(tx);

        } catch (Exception e) {
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepo.save(tx);
            throw TransactionException.externalServiceError("Transfer failed");
        }
    }


    /* ========================== HELPERS ========================== */

    private Transaction createPendingTransaction(
            String accountNumber,
            UUID customerId,
            TransactionType type,
            BigDecimal amount,
            String idempotencyKey
    ) {
        return transactionRepo.save(
                Transaction.builder()
                        .transactionId(generateTxnId())
                        .accountNumber(accountNumber)
                        .customerId(customerId)
                        .transactionType(type)
                        .amount(amount)
                        .totalAmount(amount)
                        .status(TransactionStatus.PENDING)
                        .idempotencyKey(idempotencyKey)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private void executeDebit(String accountNumber, BigDecimal amount) {
        try {
            accountClient.debit(accountNumber, amount);
        } catch (FeignException e) {
            throw TransactionException.externalServiceError("Debit operation failed");
        }
    }

    private void executeCredit(String accountNumber, BigDecimal amount) {
        try {
            accountClient.credit(accountNumber, amount);
        } catch (FeignException e) {
            throw TransactionException.externalServiceError("Credit operation failed");
        }
    }

    private BigDecimal fetchBalance(String accountNumber) {
        return accountClient.getBalance(accountNumber);
    }

    private void verifyAccountOwnership(UUID customerId, String accountNumber) {
        try {
            UUID ownerId = accountClient.getAccountOwner(accountNumber);

            if (ownerId == null || !ownerId.equals(customerId)) {
                throw TransactionException.unauthorized(
                        "You are not authorized to operate on this account"
                );
            }
        } catch (FeignException.NotFound e) {
            throw TransactionException.accountNotFound();
        } catch (FeignException e) {
            throw TransactionException.externalServiceError(
                    "Failed to verify account ownership"
            );
        }
    }

    private void validateDebit(DebitTransactionRequest req) {
        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());
    }

    private void checkTransactionLimits(String accountNumber, BigDecimal amount) {

        TransactionLimit limit =
                limitRepo.findById(accountNumber)
                        .orElse(new TransactionLimit(accountNumber));

        if (amount.compareTo(limit.getPerTransactionLimit()) > 0) {
            throw TransactionException.limitExceeded("Per transaction limit exceeded");
        }

        BigDecimal todayTotal =
                transactionRepo.findByAccountNumberAndDate(accountNumber, LocalDate.now())
                        .stream()
                        .filter(t ->
                                t.getTransactionType() == TransactionType.DEBIT ||
                                        t.getTransactionType() == TransactionType.TRANSFER)
                        .map(Transaction::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTotal.add(amount).compareTo(limit.getDailyLimit()) > 0) {
            throw TransactionException.limitExceeded("Daily limit exceeded");
        }
    }

    private DebitTransactionResponse buildDebitResponse(Transaction tx) {
        return DebitTransactionResponse.builder()
                .success(true)
                .message("SUCCESS")
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
                .message("SUCCESS")
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

        TransferMode mode = tx.getTransferMode();

        if (mode == null) {
            throw new TransactionException(
                    "TRANSFER_MODE_MISSING",
                    "Transfer mode not set for transaction " + tx.getTransactionId()
            );
        }

        return TransferTransactionResponse.builder()
                .success(tx.getStatus() == TransactionStatus.SUCCESS)
                .message(tx.getStatus().name())
                .transactionId(tx.getTransactionId())
                .transferMode(mode.name())
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

    private BigDecimal calculateTransferCharges(TransferMode mode, BigDecimal amount) {
        return switch (mode) {
            case IMPS -> BigDecimal.valueOf(5);
            case NEFT, UPI -> BigDecimal.ZERO;
            case RTGS -> amount.compareTo(BigDecimal.valueOf(200000)) > 0
                    ? BigDecimal.valueOf(30)
                    : BigDecimal.valueOf(25);
        };
    }

    private AuthUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser)) {
            throw TransactionException.unauthorized("User not authenticated");
        }
        return (AuthUser) auth.getPrincipal();
    }

    private String generateIdempotencyKey(String type, String ref, UUID customerId) {
        return type + "-" + ref + "-" + customerId + "-" + LocalDate.now();
    }

    private String generateTxnId() {
        return "TXN" + System.currentTimeMillis();
    }

    private String generateUTR() {
        return "UTR" + System.currentTimeMillis();
    }
}
