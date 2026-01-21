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
                        month.getYear(),
                        month.getMonthValue()
                );

        BigDecimal totalSpent = BigDecimal.ZERO;
        Map<TransactionCategory, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Transaction tx : transactions) {

            if (tx.getTransactionType() == TransactionType.DEBIT ||
                    tx.getTransactionType() == TransactionType.TRANSFER) {

                totalSpent = totalSpent.add(tx.getTotalAmount());

                categoryBreakdown.merge(
                        tx.getCategory(),
                        tx.getTotalAmount(),
                        BigDecimal::add
                );
            }
        }

        return TransactionAnalyticsResponse.builder()
                .month(month.toString())
                .totalSpent(totalSpent)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }
}
