package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import com.bank.transaction_service.service.TransactionLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionLimitController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionLimitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionLimitService limitService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    private void setAuth() {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    @Test
    void getLimits_success() throws Exception {
        setAuth();
        TransactionLimitResponse response =
                TransactionLimitResponse.builder()
                        .dailyLimit(BigDecimal.valueOf(50000))
                        .monthlyLimit(BigDecimal.valueOf(200000))
                        .build();

        when(limitService.get("123456"))
                .thenReturn(response);

        mockMvc.perform(get("/api/customer/limits")
                        .param("accountNumber", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dailyLimit").value(50000))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Transaction limits fetched successfully"));

        verify(limitService).get("123456");
    }

    @Test
    void updateLimits_success() throws Exception {
        setAuth();
        String json = """
                {
                    "dailyLimit": 70000,
                    "monthlyLimit": 250000
                }
                """;

        TransactionLimitResponse response =
                TransactionLimitResponse.builder()
                        .dailyLimit(BigDecimal.valueOf(70000))
                        .monthlyLimit(BigDecimal.valueOf(250000))
                        .build();

        when(limitService.update(eq("123456"), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/customer/limits")
                        .param("accountNumber", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dailyLimit").value(70000))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Transaction limits updated successfully"));

        verify(limitService).update(eq("123456"), any());
    }
    @Test
    void getLimits_unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/customer/limits")
                        .param("accountNumber", "123456"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(limitService);
    }
    @Test
    void updateLimits_notAuthenticated() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        auth.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(put("/api/customer/limits")
                        .param("accountNumber", "123456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(limitService);
    }

    @Test
    void getLimits_wrongPrincipal() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "stringUser", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/customer/limits")
                        .param("accountNumber", "123456"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(limitService);
    }

}
