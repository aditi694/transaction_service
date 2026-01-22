package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionCategory;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionAnalyticsResponse getMonthlyAnalytics(
            String accountNumber,
            YearMonth month
    ) {

        List<Transaction> transactions =
                transactionRepository.findByAccountNumberAndMonth(
                        accountNumber,
                        month.getMonthValue(),
                        month.getYear()
                );

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        for (Transaction tx : transactions) {

            if (tx.getTransactionType() == TransactionType.DEBIT ||
                    tx.getTransactionType() == TransactionType.TRANSFER) {

                totalDebit = totalDebit.add(tx.getTotalAmount());

                String category =
                        tx.getCategory() == null ? "OTHERS" : tx.getCategory().name();

                categoryMap.merge(category, tx.getTotalAmount(), BigDecimal::add);
            }

            if (tx.getTransactionType() == TransactionType.CREDIT) {
                totalCredit = totalCredit.add(tx.getTotalAmount());
            }
        }

        List<TransactionAnalyticsResponse.CategorySpend> breakdown =
                categoryMap.entrySet().stream()
                        .map(e -> TransactionAnalyticsResponse.CategorySpend.builder()
                                .category(e.getKey())
                                .amount(e.getValue())
                                .build())
                        .toList();

        return TransactionAnalyticsResponse.builder()
                .success(true)
                .accountNumber(accountNumber)
                .month(month.toString())
                .summary(
                        TransactionAnalyticsResponse.Summary.builder()
                                .totalDebit(totalDebit)
                                .totalCredit(totalCredit)
                                .netFlow(totalCredit.subtract(totalDebit))
                                .transactionCount(transactions.size())
                                .build()
                )
                .categoryBreakdown(breakdown)
                .build();

    }
}
