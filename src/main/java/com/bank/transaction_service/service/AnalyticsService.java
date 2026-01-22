package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;

import java.time.YearMonth;

public interface AnalyticsService {
    TransactionAnalyticsResponse getMonthlyAnalytics(
            String accountNumber,
            YearMonth month
    );
}
