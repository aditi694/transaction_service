package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;
import com.bank.transaction_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/analytics")
    public TransactionAnalyticsResponse analytics(
            @RequestParam String accountNumber,
            @RequestParam String month
    ) {
        YearMonth yearMonth = YearMonth.parse(month);

        return analyticsService.getMonthlyAnalytics(accountNumber, yearMonth);
    }
}
