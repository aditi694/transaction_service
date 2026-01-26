package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.exception.TransactionException;
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

    @Override
    public TransactionHistoryResponse getHistory(String accountNumber, int limit, int page) {

        PageRequest pageable = PageRequest.of(page - 1, limit);
        Page<Transaction> txPage = transactionRepo
                .findByAccountNumberOrderByCreatedAtDesc(accountNumber, pageable);

        if (txPage.isEmpty()) {
            return TransactionHistoryResponse.builder()
                    .success(true)
                    .message("No transactions found yet")
                    .description("You haven't made any transactions yet. Start by depositing money or making a transfer!")
                    .page(page)
                    .limit(limit)
                    .total(0)
                    .transactions(List.of())
                    .build();
        }

        List<TransactionResponse> data = txPage.getContent().stream()
                .map(this::mapTransaction)
                .toList();

        return TransactionHistoryResponse.builder()
                .success(true)
                .message(String.format("Showing %d of %d transactions",
                        data.size(), txPage.getTotalElements()))
                .description(String.format("Transaction history for account %s",
                        maskAccount(accountNumber)))
                .transactions(data)
                .total(txPage.getTotalElements())
                .page(page)
                .limit(limit)
                .hasMore(txPage.hasNext())
                .build();
    }

    @Override
    public MiniStatementResponse miniStatement(String accountNumber) {

        List<Transaction> last5 = transactionRepo
                .findTop5ByAccountNumberOrderByCreatedAtDesc(accountNumber);

        if (last5.isEmpty()) {
            return MiniStatementResponse.builder()
                    .success(true)
                    .message("No transactions available")
                    .description("Your account is active but has no transaction history yet")
                    .accountNumber(maskAccount(accountNumber))
                    .currentBalance(BigDecimal.ZERO)
                    .lastTransactions(List.of())
                    .build();
        }

        List<MiniStatementResponse.MiniTxn> miniTxns = last5.stream()
                .map(tx -> MiniStatementResponse.MiniTxn.builder()
                        .date(tx.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                        .description(tx.getDescription())
                        .amount(tx.getTransactionType().name().equals("DEBIT")
                                ? tx.getTotalAmount().negate()
                                : tx.getTotalAmount())
                        .balance(tx.getBalanceAfter())
                        .type(tx.getTransactionType().name())
                        .build())
                .toList();

        BigDecimal balance = last5.get(0).getBalanceAfter();

        return MiniStatementResponse.builder()
                .success(true)
                .message("Mini statement generated successfully")
                .description(String.format("Last %d transactions for your account", miniTxns.size()))
                .accountNumber(maskAccount(accountNumber))
                .currentBalance(balance)
                .lastTransactions(miniTxns)
                .build();
    }

    private TransactionResponse mapTransaction(Transaction tx) {
        String amountDisplay = tx.getTransactionType().name().equals("DEBIT")
                ? "- ₹" + tx.getTotalAmount()
                : "+ ₹" + tx.getTotalAmount();

        return TransactionResponse.builder()
                .transactionId(tx.getTransactionId())
                .date(tx.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .time(tx.getCreatedAt().format(DateTimeFormatter.ofPattern("hh:mm a")))
                .type(tx.getTransactionType().name())
                .category(tx.getCategory() != null ? tx.getCategory().name() : "OTHER")
                .description(tx.getDescription())
                .amount(amountDisplay)
                .balanceAfter(tx.getBalanceAfter())
                .status(tx.getStatus().name())
                .statusMessage(getTransactionStatusMessage(tx))
                .build();
    }

    private String getTransactionStatusMessage(Transaction tx) {
        return switch (tx.getStatus()) {
            case SUCCESS -> "Transaction completed successfully";
            case PENDING -> "Transaction is being processed";
            case FAILED -> "Transaction failed. Amount will be refunded if debited";
            default -> "Transaction status: " + tx.getStatus();
        };
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) return "****";
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    @Override
    public TransactionDetailResponse getTransaction(String transactionId) {
        Transaction tx = transactionRepo.findByTransactionId(transactionId)
                .orElseThrow(TransactionException::transactionNotFound);

        return TransactionDetailResponse.builder()
                .transactionId(tx.getTransactionId())
                .amount(tx.getTotalAmount())
                .status(tx.getStatus().name())
                .build();
    }

    @Override
    public TransactionStatusResponse getStatus(String transactionId) {
        Transaction tx = transactionRepo.findByTransactionId(transactionId)
                .orElseThrow(TransactionException::transactionNotFound);

        return TransactionStatusResponse.builder()
                .transactionId(tx.getTransactionId())
                .status(tx.getStatus().name())
                .build();
    }
}