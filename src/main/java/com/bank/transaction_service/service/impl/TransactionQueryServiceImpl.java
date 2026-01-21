package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final TransactionRepository transactionRepo;

    @Override
    public TransactionHistoryResponse getHistory(
            String accountNumber, int limit, int page) {

        var pageable = PageRequest.of(page - 1, limit);
        var txPage =
                transactionRepo.findByAccountNumberOrderByCreatedAtDesc(
                        accountNumber, pageable
                );

        List<TransactionSummary> data =
                txPage.getContent().stream()
                        .map(tx -> TransactionSummary.builder()
                                .transactionId(tx.getTransactionId())
                                .amount(tx.getTotalAmount())
                                .type(tx.getTransactionType().name())
                                .status(tx.getStatus().name())
                                .build())
                        .toList();

        return TransactionHistoryResponse.builder()
                .success(true)
                .transactions(data)
                .total(txPage.getTotalElements())
                .page(page)
                .limit(limit)
                .build();
    }

    @Override
    public TransactionDetailResponse getTransaction(String transactionId) {

        Transaction tx =
                transactionRepo.findByTransactionId(transactionId)
                        .orElseThrow(TransactionException::transactionNotFound);

        return TransactionDetailResponse.builder()
                .transactionId(tx.getTransactionId())
                .amount(tx.getTotalAmount())
                .status(tx.getStatus().name())
                .build();
    }

    @Override
    public TransactionStatusResponse getStatus(String transactionId) {

        Transaction tx =
                transactionRepo.findByTransactionId(transactionId)
                        .orElseThrow(TransactionException::transactionNotFound);

        return TransactionStatusResponse.builder()
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .build();
    }

    @Override
    public MiniStatementResponse miniStatement(String accountNumber) {

        List<Transaction> last5 =
                transactionRepo
                        .findTop5ByAccountNumberOrderByCreatedAtDesc(
                                accountNumber
                        );

        return MiniStatementResponse.builder()
                .accountNumber(accountNumber)
                .transactions(last5)
                .build();
    }
}
