package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.response.MiniStatementResponse;
import com.bank.transaction_service.dto.response.TransactionHistoryResponse;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private TransactionQueryServiceImpl service;

    @Test
    void history_empty() {

        Page<Transaction> page =
                new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

        when(repository.findByAccountNumberOrderByCreatedAtDesc(eq("ACC1"), any()))
                .thenReturn(page);

        TransactionHistoryResponse response =
                service.getHistory("ACC1", 5, 1);

        assertTrue(response.getTransactions().isEmpty());
        assertEquals(0, response.getTotal());
        assertFalse(response.isHasMore());
    }

    @Test
    void history_withData() {

        Transaction tx = buildTxn(TransactionType.DEBIT, TransactionStatus.SUCCESS);

        Page<Transaction> page =
                new PageImpl<>(List.of(tx), PageRequest.of(0, 5), 1);

        when(repository.findByAccountNumberOrderByCreatedAtDesc(eq("ACC1"), any()))
                .thenReturn(page);

        TransactionHistoryResponse response =
                service.getHistory("ACC1", 5, 1);

        assertEquals(1, response.getTransactions().size());
        assertTrue(response.getTransactions().get(0).getAmount().contains("-"));
    }

    @ParameterizedTest
    @CsvSource({
            "SUCCESS,Transaction completed successfully",
            "IN_PROGRESS,Transaction is being initiated",
            "PENDING,Transaction is being processed",
            "FAILED,Transaction failed. Amount will be refunded if debited"
    })
    void history_statusMessages_exact(TransactionStatus status, String expectedMessage) {

        Transaction tx = Transaction.builder()
                .transactionId("TXN1")
                .transactionType(TransactionType.CREDIT)
                .status(status)
                .description("Test")
                .totalAmount(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        Page<Transaction> page =
                new PageImpl<>(List.of(tx), PageRequest.of(0, 5), 1);

        when(repository.findByAccountNumberOrderByCreatedAtDesc(eq("ACC1"), any()))
                .thenReturn(page);

        TransactionHistoryResponse response =
                service.getHistory("ACC1", 5, 1);

        String actualMessage =
                response.getTransactions().get(0).getStatusMessage();

        assertEquals(expectedMessage, actualMessage);
    }


    @Test
    void mini_empty() {

        when(repository.findTop5ByAccountNumberOrderByCreatedAtDesc("ACC1"))
                .thenReturn(List.of());

        when(accountClient.getBalance("ACC1"))
                .thenReturn(BigDecimal.valueOf(5000));

        MiniStatementResponse response =
                service.miniStatement("ACC1");

        assertTrue(response.getLastTransactions().isEmpty());
        assertEquals("****ACC1".substring(4), response.getAccountNumber().substring(4));
    }

    @Test
    void mini_withData() {

        Transaction debit = buildTxn(TransactionType.DEBIT, TransactionStatus.SUCCESS);
        Transaction credit = buildTxn(TransactionType.CREDIT, TransactionStatus.SUCCESS);

        when(repository.findTop5ByAccountNumberOrderByCreatedAtDesc("ACC1"))
                .thenReturn(List.of(debit, credit));

        when(accountClient.getBalance("ACC1"))
                .thenReturn(BigDecimal.valueOf(10000));

        MiniStatementResponse response =
                service.miniStatement("ACC1");

        assertEquals(2, response.getLastTransactions().size());
        assertTrue(response.getLastTransactions().get(0).getAmount().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(response.getLastTransactions().get(1).getAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void mini_maskShortAccount() {

        when(repository.findTop5ByAccountNumberOrderByCreatedAtDesc("12"))
                .thenReturn(List.of());

        when(accountClient.getBalance("12"))
                .thenReturn(BigDecimal.ZERO);

        MiniStatementResponse response =
                service.miniStatement("12");

        assertEquals("****", response.getAccountNumber());
    }

    @Test
    void mini_maskNullAccount() {

        when(repository.findTop5ByAccountNumberOrderByCreatedAtDesc(null))
                .thenReturn(List.of());

        when(accountClient.getBalance(null))
                .thenReturn(BigDecimal.ZERO);

        MiniStatementResponse response =
                service.miniStatement(null);

        assertEquals("****", response.getAccountNumber());
    }

    private Transaction buildTxn(TransactionType type, TransactionStatus status) {

        return Transaction.builder()
                .transactionId("TXN1")
                .transactionType(type)
                .status(status)
                .description("Test")
                .totalAmount(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
