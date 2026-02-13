package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import com.bank.transaction_service.service.AnalyticsService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private AnalyticsService analyticsService;
    @Test
    void analytics_success() throws Exception {
        TransactionAnalyticsResponse response =
                TransactionAnalyticsResponse.builder()
                        .accountNumber("123456")
                        .month("2026-01")
                        .build();

        when(analyticsService.getMonthlyAnalytics(any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions/analytics")
                        .param("accountNumber", "123456")
                        .param("month", "2026-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountNumber").value("123456"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Transaction analytics fetched successfully"));

        verify(analyticsService)
                .getMonthlyAnalytics("123456", YearMonth.of(2026, 1));
    }

    @Test
    void analytics_exception() throws Exception {

        when(analyticsService.getMonthlyAnalytics(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/transactions/analytics")
                        .param("accountNumber", "123456")
                        .param("month", "2026-01"))
                .andExpect(status().isInternalServerError());
    }
}