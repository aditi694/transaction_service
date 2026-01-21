package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.enums.TransferMode;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.NotificationService;
import com.bank.transaction_service.service.TransactionService;
import com.bank.transaction_service.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final NotificationService notificationService;

    @Override
    public DebitTransactionResponse debit(DebitTransactionRequest req) {

        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getAccountNumber())
                .transactionType(TransactionType.DEBIT)
                .category(req.getCategory())
                .amount(req.getAmount())
                .charges(BigDecimal.ZERO)
                .totalAmount(req.getAmount())
                .description(req.getDescription())
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);
        notificationService.sendTransactionAlert(tx);

        return DebitTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .build();
    }

    @Override
    public CreditTransactionResponse credit(CreditTransactionRequest req) {

        TransactionValidator.validateAccountNumber(req.getAccountNumber());
        TransactionValidator.validateAmount(req.getAmount());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getAccountNumber())
                .transactionType(TransactionType.CREDIT)
                .category(req.getCategory())
                .amount(req.getAmount())
                .charges(BigDecimal.ZERO)
                .totalAmount(req.getAmount())
                .description(req.getDescription())
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);
        notificationService.sendTransactionAlert(tx);

        return CreditTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .build();
    }

    @Override
    public TransferTransactionResponse transfer(TransferTransactionRequest req) {

        TransactionValidator.validateTransfer(
                req.getFromAccount(),
                req.getToAccount()
        );

        BigDecimal total = req.getAmount().add(req.getCharges());

        Transaction tx = Transaction.builder()
                .transactionId(generateTxnId())
                .accountNumber(req.getFromAccount())
                .toAccount(req.getToAccount())
                .transactionType(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .charges(req.getCharges())
                .totalAmount(total)
                .transferMode(TransferMode.valueOf(req.getTransferType()))
                .description(req.getDescription())
                .utrNumber(generateUTR())
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepo.save(tx);
        notificationService.sendTransactionAlert(tx);

        return TransferTransactionResponse.builder()
                .success(true)
                .transactionId(tx.getTransactionId())
                .utrNumber(tx.getUtrNumber())
                .build();
    }

    private String generateTxnId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12);
    }

    private String generateUTR() {
        return "UTR" + System.currentTimeMillis();
    }
}
