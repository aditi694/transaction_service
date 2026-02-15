package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.MiniStatementResponse;
import com.bank.transaction_service.dto.response.TransactionHistoryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import com.bank.transaction_service.service.TransactionQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TransactionQueryController.class)
class TransactionQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionQueryService queryService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void history_success() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        TransactionHistoryResponse mockResponse =
                TransactionHistoryResponse.builder().build();

        when(queryService.getHistory("1234567890", 20, 1))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/customer/transactions")
                        .param("account_number", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Transaction history fetched successfully"));

        verify(queryService)
                .getHistory("1234567890", 20, 1);
    }

    @Test
    void miniStatement_success() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        MiniStatementResponse mockResponse =
                MiniStatementResponse.builder().build();

        when(queryService.miniStatement("1234567890"))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/customer/mini-statement")
                        .param("account_number", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Mini statement generated successfully"));

        verify(queryService)
                .miniStatement("1234567890");
    }

    @Test
    void history_shouldFail_whenNoAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/customer/transactions")
                        .param("account_number", "1234567890"))
                .andExpect(status().isForbidden());
    }
    @Test
    void history_shouldFail_whenNotAuthenticated() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        auth.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/customer/transactions")
                        .param("account_number", "1234567890"))
                .andExpect(status().isForbidden());
    }
    @Test
    void history_shouldFail_whenPrincipalIsNotAuthUser() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "someStringUser", null, null);

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/customer/transactions")
                        .param("account_number", "1234567890"))
                .andExpect(status().isForbidden());
    }

}
