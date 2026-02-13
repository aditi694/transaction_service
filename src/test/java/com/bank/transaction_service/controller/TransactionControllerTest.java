package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.CreditTransactionResponse;
import com.bank.transaction_service.dto.response.DebitTransactionResponse;
import com.bank.transaction_service.dto.response.TransactionStatusResponse;
import com.bank.transaction_service.dto.response.TransferInitiatedResponse;
import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.Frequency;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import com.bank.transaction_service.service.TransactionService;
import com.google.common.base.Verify;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private TransactionService transactionService;

    @Test
    void debit_success() throws Exception {
        DebitTransactionResponse response =
                DebitTransactionResponse.builder()
                        .success(true)
                        .message("Success")
                        .transactionId("TXN-1")
                        .status("COMPLETED")
                        .build();

        when(transactionService.debit(any())).thenReturn(response);

        String json = """
                {
                  "accountNumber":"12345",
                  "amount":1000,
                  "category":"FOOD",
                  "description":"Lunch"
                }
                """;

        mockMvc.perform(post("/api/customer/transaction/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Debit transaction initiated"))
                .andExpect(jsonPath("$.data.transactionId")
                        .value("TXN-1"));

        verify(transactionService).debit(any());
    }

    @Test
    void credit_success() throws Exception {
        CreditTransactionResponse response = CreditTransactionResponse.builder()
                .success(true)
                .transactionId("TXN-2")
                .status("COMPLETED")
                .build();
        String json = """
                { "accountNUmber":"ACC123",
                "amount":1000,
                "category":"SALARY",
                "description":"First salary"
                }
                """;
        when(transactionService.credit(any()))
                .thenReturn(response);
        mockMvc.perform(post("/api/customer/transaction/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg").value("Credit transaction initiated"))
                .andExpect(jsonPath("$.data.transactionId").value("TXN-2"));
        verify(transactionService).credit(any());
    }

    @Test
    void transfer_success() throws Exception {
        TransferInitiatedResponse response =
                TransferInitiatedResponse.builder()
                        .success(true)
                        .transactionId("TXN-3")
                        .status("PENDING")
                        .timestamp(LocalDateTime.now())
                        .build();

        when(transactionService.transfer(any()))
                .thenReturn(response);

        String json = """
                {
                  "fromAccount":"123",
                  "toAccount":"456",
                  "amount":1000,
                  "transferType":"IMPS",
                  "description":"Rent"
                }
                """;

        mockMvc.perform(post("/api/customer/transaction/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Transfer transaction initiated"))
                .andExpect(jsonPath("$.data.transactionId")
                        .value("TXN-3"));

        verify(transactionService).transfer(any());
    }
    @Test
    void getStatus_success() throws Exception {
        TransactionStatusResponse response =
                TransactionStatusResponse.builder()
                        .transactionId("TXN-1")
                        .status("COMPLETED")
                        .amount(BigDecimal.valueOf(1000))
                        .build();

        when(transactionService.getStatus("TXN-1"))
                .thenReturn(response);

        mockMvc.perform(get("/api/customer/transaction/TXN-1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Transaction status fetched successfully"))
                .andExpect(jsonPath("$.data.transactionId")
                        .value("TXN-1"));

        verify(transactionService).getStatus("TXN-1");
    }
    @Test
    void getStatus_notFound() throws Exception {
        when(transactionService.getStatus("TXN-999"))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/customer/transaction/TXN-999/status"))
                .andExpect(status().isInternalServerError());
    }
}