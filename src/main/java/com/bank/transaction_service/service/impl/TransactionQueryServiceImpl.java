package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final TransactionRepository transactionRepo;
    private final AccountClient accountClient;

    @Override
    public TransactionHistoryResponse getHistory(String accountNumber, int limit, int page) {

        PageRequest pageable = PageRequest.of(page - 1, limit);

        Page<Transaction> txPage =
                transactionRepo.findByAccountNumberOrderByCreatedAtDesc(
                        accountNumber, pageable
                );

        if (txPage.isEmpty()) {
            return TransactionHistoryResponse.builder()
                    .success(true)
                    .message("No transactions found yet")
                    .description("You haven't made any transactions yet")
                    .page(page)
                    .limit(limit)
                    .total(0)
                    .hasMore(false)
                    .transactions(List.of())
                    .build();
        }

        List<TransactionResponse> transactions =
                txPage.getContent()
                        .stream()
                        .map(this::mapTransaction)
                        .toList();

        return TransactionHistoryResponse.builder()
                .success(true)
                .message("Transaction history fetched successfully")
                .description("Transaction history for account " + maskAccount(accountNumber))
                .transactions(transactions)
                .total(txPage.getTotalElements())
                .page(page)
                .limit(limit)
                .hasMore(txPage.hasNext())
                .build();
    }

    // ================= MINI STATEMENT =================

    @Override
    public MiniStatementResponse miniStatement(String accountNumber) {

        List<Transaction> last5 =
                transactionRepo.findTop5ByAccountNumberOrderByCreatedAtDesc(accountNumber);

        BigDecimal currentBalance =
                accountClient.getBalance(accountNumber);

        if (last5.isEmpty()) {
            return MiniStatementResponse.builder()
                    .success(true)
                    .message("No transactions available")
                    .description("Your account is active but has no transaction history yet")
                    .accountNumber(maskAccount(accountNumber))
                    .currentBalance(currentBalance)
                    .lastTransactions(List.of())
                    .build();
        }

        List<MiniStatementResponse.MiniTxn> miniTxns =
                last5.stream()
                        .map(tx -> MiniStatementResponse.MiniTxn.builder()
                                .date(tx.getCreatedAt()
                                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                                .description(tx.getDescription())
                                .amount(
                                        tx.getTransactionType() == TransactionType.DEBIT
                                                ? tx.getTotalAmount().negate()
                                                : tx.getTotalAmount()
                                )
                                .type(tx.getTransactionType().name())
                                .build()
                        )
                        .toList();

        return MiniStatementResponse.builder()
                .success(true)
                .message("Mini statement generated successfully")
                .description("Last " + miniTxns.size() + " transactions")
                .accountNumber(maskAccount(accountNumber))
                .currentBalance(currentBalance)
                .lastTransactions(miniTxns)
                .build();
    }

    // ================= MAPPER =================

    private TransactionResponse mapTransaction(Transaction tx) {

        String amountDisplay =
                tx.getTransactionType() == TransactionType.DEBIT
                        ? "- ₹" + tx.getTotalAmount()
                        : "+ ₹" + tx.getTotalAmount();

        return TransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .date(tx.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .time(tx.getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("hh:mm a")))
                .type(tx.getTransactionType().name())
                .description(tx.getDescription())
                .amount(amountDisplay)
                .status(tx.getStatus().name())
                .statusMessage(getTransactionStatusMessage(tx))
                .build();
    }

    private String getTransactionStatusMessage(Transaction tx) {
        return switch (tx.getStatus()) {
            case SUCCESS -> "Transaction completed successfully";
            case IN_PROGRESS -> "Transaction is being initiated";
            case PENDING -> "Transaction is being processed";
            case FAILED -> "Transaction failed. Amount will be refunded if debited";
        };
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}