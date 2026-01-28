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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final TransactionLimitRepository limitRepo;
    private final AccountClient accountClient;
    private final TransactionSagaService sagaService;


    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {

        AuthUser user = currentUser();
        verifyAccountOwnership(user.getCustomerId(), req.getAccountNumber());

        BigDecimal amount = req.getAmount();
        BigDecimal before = fetchBalance(req.getAccountNumber());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getAccountNumber())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.CREDIT)
                .amount(amount)
                .charges(BigDecimal.ZERO)
                .totalAmount(amount)
                .balanceBefore(before)
                .status(TransactionStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(
                tx.getTransactionId(),
                amount,
                null,
                req.getAccountNumber()
        );

        sagaService.credit(saga, req.getAccountNumber(), amount);
        sagaService.complete(saga);

        BigDecimal after = fetchBalance(req.getAccountNumber());

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setBalanceAfter(after);
        transactionRepo.save(tx);

        return CreditTransactionResponse.builder()
                .success(true)
                .message("Amount credited successfully")
                .transactionId(tx.getTransactionId())
                .amount(amount)
                .previousBalance(before)
                .currentBalance(after)
                .status("SUCCESS")
                .timestamp(tx.getCreatedAt())
                .build();
    }


    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req) {

        AuthUser user = currentUser();
        verifyAccountOwnership(user.getCustomerId(), req.getAccountNumber());

        BigDecimal amount = req.getAmount();
        BigDecimal before = fetchBalance(req.getAccountNumber());

        checkTransactionLimits(req.getAccountNumber(), amount);

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getAccountNumber())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.DEBIT)
                .amount(amount)
                .charges(BigDecimal.ZERO)
                .totalAmount(amount)
                .balanceBefore(before)
                .status(TransactionStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(
                tx.getTransactionId(),
                amount,
                req.getAccountNumber(),
                null
        );

        sagaService.debit(saga, req.getAccountNumber(), amount);
        sagaService.complete(saga);

        BigDecimal after = fetchBalance(req.getAccountNumber());

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setBalanceAfter(after);
        transactionRepo.save(tx);

        return DebitTransactionResponse.builder()
                .success(true)
                .message("Transaction successful")
                .transactionId(tx.getTransactionId())
                .amount(amount)
                .charges(BigDecimal.ZERO)
                .totalDeducted(amount)
                .previousBalance(before)
                .currentBalance(after)
                .status("SUCCESS")
                .timestamp(tx.getCreatedAt())
                .build();
    }

    @Override
    public TransferTransactionResponse transfer(TransferTransactionRequest req) {

        AuthUser user = currentUser();
        verifyAccountOwnership(user.getCustomerId(), req.getFromAccount());

        BigDecimal amount = req.getAmount();
        TransferMode mode = TransferMode.valueOf(req.getTransferType());

        BigDecimal charges = calculateTransferCharges(mode, amount);
        BigDecimal totalDeducted = amount.add(charges);

        checkTransactionLimits(req.getFromAccount(), totalDeducted);

        BigDecimal before = fetchBalance(req.getFromAccount());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .toAccount(req.getToAccount())
                .customerId(user.getCustomerId())
                .transactionType(TransactionType.TRANSFER)
                .transferMode(mode)
                .amount(amount)
                .charges(charges)
                .totalAmount(totalDeducted)
                .balanceBefore(before)
                .status(TransactionStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);

        TransactionSaga saga = sagaService.start(
                tx.getTransactionId(),
                totalDeducted,
                req.getFromAccount(),
                req.getToAccount()
        );

        sagaService.debit(saga, req.getFromAccount(), totalDeducted);
        sagaService.credit(saga, req.getToAccount(), amount);
        sagaService.complete(saga);

        BigDecimal after = fetchBalance(req.getFromAccount());

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setBalanceAfter(after);
        tx.setUtrNumber(generateUTR());
        transactionRepo.save(tx);

        return TransferTransactionResponse.builder()
                .success(true)
                .message("Transfer completed successfully")
                .transactionId(tx.getTransactionId())
                .fromAccount(req.getFromAccount())
                .toAccount(req.getToAccount())
                .transferMode(mode.name())
                .amount(amount)
                .charges(charges)
                .totalDeducted(totalDeducted)
                .senderBalanceBefore(before)
                .senderBalanceAfter(after)
                .status("SUCCESS")
                .utrNumber(tx.getUtrNumber())
                .timestamp(tx.getCreatedAt())
                .build();
    }


    private BigDecimal fetchBalance(String accountNumber) {
        try {
            return accountClient.getBalance(accountNumber);
        } catch (FeignException e) {
            throw TransactionException.externalServiceError("Account service unavailable");
        }
    }

    private void verifyAccountOwnership(UUID customerId, String accountNumber) {
        try {
            UUID owner = accountClient.getAccountOwner(accountNumber);
            if (!owner.equals(customerId)) {
                throw TransactionException.unauthorized(
                        "You are not authorized to operate this account"
                );
            }
        } catch (FeignException e) {
            throw TransactionException.externalServiceError(
                    "Unable to verify account ownership"
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

    private AuthUser currentUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof AuthUser authUser) {
            return authUser;
        }
        throw TransactionException.unauthorized("User not authenticated");
    }

    private String generateTxnId() {
        return "TXN" + System.currentTimeMillis();
    }

    private String generateUTR() {
        return "UTR" + System.currentTimeMillis();
    }
}
