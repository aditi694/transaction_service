package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionCategory;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private AnalyticsServiceImpl service;

    @Test
    void getMonthlyAnalytics_CalculateCorrectly() {
        Transaction debit = Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .totalAmount(new BigDecimal("1000"))
                .category(TransactionCategory.FOOD)
                .build();

        Transaction credit = Transaction.builder()
                .transactionType(TransactionType.CREDIT)
                .totalAmount(new BigDecimal("2000"))
                .build();

        Transaction transfer = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .totalAmount(new BigDecimal("500"))
                .category(null)
                .build();

        when(repository.findByAccountNumberAndMonth("123", 1, 2026))
                .thenReturn(List.of(debit, credit, transfer));

        TransactionAnalyticsResponse response =
                service.getMonthlyAnalytics("123",
                        YearMonth.of(2026, 1));

        assertNotNull(response);
        assertEquals("123", response.getAccountNumber());
        assertEquals("2026-01", response.getMonth());

        assertEquals(new BigDecimal("1500"),
                response.getSummary().getTotalDebit());

        assertEquals(new BigDecimal("2000"),
                response.getSummary().getTotalCredit());

        assertEquals(new BigDecimal("500"),
                response.getSummary().getNetFlow());

        assertEquals(3,
                response.getSummary().getTransactionCount());

        assertEquals(2,
                response.getCategoryBreakdown().size());

        verify(repository)
                .findByAccountNumberAndMonth("123", 1, 2026);
    }

    @Test
    void getMonthlyAnalytics_emptyTransactions() {
        when(repository.findByAccountNumberAndMonth("123", 1, 2026))
                .thenReturn(List.of());

        TransactionAnalyticsResponse response =
                service.getMonthlyAnalytics("123",
                        YearMonth.of(2026, 1));

        assertEquals(BigDecimal.ZERO,
                response.getSummary().getTotalDebit());

        assertEquals(BigDecimal.ZERO,
                response.getSummary().getTotalCredit());

        assertEquals(BigDecimal.ZERO,
                response.getSummary().getNetFlow());

        assertEquals(0,
                response.getSummary().getTransactionCount());

        assertTrue(response.getCategoryBreakdown().isEmpty());

        verify(repository)
                .findByAccountNumberAndMonth("123", 1, 2026);
    }

    @Test
    void getMonthlyAnalytics_nullCategory() {
        Transaction tx = Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .totalAmount(new BigDecimal("100"))
                .category(null)
                .build();

        when(repository.findByAccountNumberAndMonth("123", 1, 2026))
                .thenReturn(List.of(tx));

        TransactionAnalyticsResponse response =
                service.getMonthlyAnalytics("123",
                        YearMonth.of(2026, 1));

        assertEquals(1,
                response.getCategoryBreakdown().size());

        assertEquals("OTHERS",
                response.getCategoryBreakdown().get(0).getCategory());

        verify(repository)
                .findByAccountNumberAndMonth("123", 1, 2026);
    }
}
