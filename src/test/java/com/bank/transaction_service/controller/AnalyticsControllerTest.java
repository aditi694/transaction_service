package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;
import com.bank.transaction_service.service.AnalyticsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.YearMonth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    @Test
    public void testAnalytics() {

        String accountNumber = "ACC123";
        String month = "2026-02";  // must match YearMonth format

        TransactionAnalyticsResponse response =
                new TransactionAnalyticsResponse();

        when(analyticsService.getMonthlyAnalytics(
                eq(accountNumber),
                any(YearMonth.class)
        )).thenReturn(response);

        ResponseEntity<BaseResponse<TransactionAnalyticsResponse>> result =
                analyticsController.analytics(accountNumber, month);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(
                "Transaction analytics fetched successfully",
                result.getBody().getResultInfo().getResultMsg()
        );
    }
}
