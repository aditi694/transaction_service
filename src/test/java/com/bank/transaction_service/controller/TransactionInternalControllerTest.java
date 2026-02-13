package com.bank.transaction_service.controller;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionRepository repo;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void totalDebit_success() throws Exception {
        UUID customerId = UUID.randomUUID();

        Transaction debit = Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .amount(BigDecimal.valueOf(1000))
                .build();

        Transaction transfer = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .amount(BigDecimal.valueOf(500))
                .build();

        Transaction credit = Transaction.builder()
                .transactionType(TransactionType.CREDIT)
                .amount(BigDecimal.valueOf(2000))
                .build();

        when(repo.findByCustomerId(customerId))
                .thenReturn(List.of(debit, transfer, credit));

        mockMvc.perform(get("/api/internal/transactions/total-debit")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1500.0))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Total debit calculated successfully"));
    }

    @Test
    void totalDebit_emptyList() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(repo.findByCustomerId(customerId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/internal/transactions/total-debit")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0.0));
    }
}
