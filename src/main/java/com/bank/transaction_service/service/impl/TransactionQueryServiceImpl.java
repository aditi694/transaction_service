package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final TransactionRepository transactionRepo;



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
    public TransactionHistoryResponse getHistory(
            String accountNumber, int limit, int page) {

        var pageable = PageRequest.of(page - 1, limit);

        var txPage =
                transactionRepo.findByAccountNumberOrderByCreatedAtDesc(
                        accountNumber, pageable
                );

        List<TransactionResponse> data =
                txPage.getContent().stream()
                        .map(tx -> TransactionResponse.builder()
                                .transactionId(tx.getTransactionId())
                                .date(tx.getCreatedAt().toLocalDate().toString())
                                .time(tx.getCreatedAt().toLocalTime().toString())
                                .type(tx.getTransactionType().name())
                                .category(String.valueOf(tx.getCategory()))
                                .description(tx.getDescription())
                                .amount(String.valueOf(tx.getTotalAmount()))
                                .balanceAfter(tx.getBalanceAfter())
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
    public MiniStatementResponse miniStatement(String accountNumber) {

        List<Transaction> last5 =
                transactionRepo
                        .findTop5ByAccountNumberOrderByCreatedAtDesc(accountNumber);

        List<MiniStatementResponse.MiniTxn> miniTxns =
                last5.stream()
                        .map(tx -> MiniStatementResponse.MiniTxn.builder()
                                .date(tx.getCreatedAt().toLocalDate().toString())
                                .description(tx.getDescription())
                                .amount(
                                        tx.getTransactionType().name().equals("DEBIT")
                                                ? tx.getTotalAmount().negate()
                                                : tx.getTotalAmount()
                                )
                                .balance(tx.getBalanceAfter())
                                .build())
                        .toList();

        BigDecimal balance =
                last5.isEmpty()
                        ? BigDecimal.ZERO
                        : last5.get(0).getBalanceAfter();

        return MiniStatementResponse.builder()
                .success(true)
                .accountNumber(accountNumber)
                .currentBalance(balance)
                .lastTransactions(miniTxns)
                .build();
    }

}
