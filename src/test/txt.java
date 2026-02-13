package com.bank.account_service.controller;

import com.bank.account_service.controller.AccountController;
import com.bank.account_service.dto.account.AccountDashboardResponse;
import com.bank.account_service.dto.account.BalanceResponse;
import com.bank.account_service.dto.account.ChangePasswordResponse;
import com.bank.account_service.dto.auth.LoginResponse;
import com.bank.account_service.exception.BusinessException;
import com.bank.account_service.security.AuthUser;
import com.bank.account_service.security.JwtFilter;
import com.bank.account_service.security.JwtUtil;
import com.bank.account_service.security.SecurityUtil;
import com.bank.account_service.service.AccountService;
import com.bank.account_service.service.BalanceService;
import com.bank.account_service.service.DashboardService;
import com.bank.account_service.service.PasswordService;
import com.bank.account_service.util.AppConstants;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private PasswordService passwordService;
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @Test
    void login_success() throws Exception {

        String json = """
        {
          "accountNumber": "ACC123",
          "password": "pass"
        }
        """;

        LoginResponse response = LoginResponse.builder()
                .success(true)
                .token("jwt-token")
                .requiresPasswordChange(false)
                .build();

        when(accountService.login(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/account/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value(AppConstants.SUCCESS_MSG))
                .andExpect(jsonPath("$.resultInfo.resultCode")
                        .value(AppConstants.SUCCESS_CODE));

        verify(accountService).login(any());
    }

    @Test
    void getBalance_success() throws Exception {

        UUID accountId = UUID.randomUUID();

        BalanceResponse response = BalanceResponse.success(
                "OK", accountId,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(1500),
                "INR"
        );
// we can use here with userContext by making the security util into component -- @Component, public class UserContext
        try (MockedStatic<SecurityUtil> securityMock =
                     Mockito.mockStatic(SecurityUtil.class)) {

            securityMock.when(SecurityUtil::getCurrentAccountId)
                    .thenReturn(accountId);

            when(balanceService.getBalance(accountId))
                    .thenReturn(response);

            mockMvc.perform(get("/api/account/balance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accountId")
                            .value(accountId.toString()));

            verify(balanceService).getBalance(accountId);
        }
    }
    @Test
    void dashboard_success() throws Exception {

        AuthUser user = AuthUser.builder()
                .accountId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .role("ROLE_CUSTOMER")
                .build();

        AccountDashboardResponse dashboard =
                AccountDashboardResponse.builder()
                        .customerName("Aditi")
                        .build();

        try (MockedStatic<SecurityUtil> securityMock =
                     Mockito.mockStatic(SecurityUtil.class)) {

            securityMock.when(SecurityUtil::getCurrentUser)
                    .thenReturn(user);

            when(dashboardService.getDashboard(user))
                    .thenReturn(dashboard);

            mockMvc.perform(get("/api/account/dashboard"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customerName")
                            .value("Aditi"));

            verify(dashboardService).getDashboard(user);
        }
    }
    @Test
    void changePassword_success() throws Exception {

        UUID accountId = UUID.randomUUID();

        ChangePasswordResponse response =
                ChangePasswordResponse.builder()
                        .success(true)
                        .message("Password changed successfully")
                        .build();

        String json = """
            {
              "oldPassword": "old",
              "newPassword": "new"
            }
            """;

        try (MockedStatic<SecurityUtil> securityMock =
                     Mockito.mockStatic(SecurityUtil.class)) {

            securityMock.when(SecurityUtil::getCurrentAccountId)
                    .thenReturn(accountId);

            when(passwordService.changePassword(eq(accountId), any()))
                    .thenReturn(response);

            mockMvc.perform(post("/api/account/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultInfo.resultMsg")
                            .value("Password changed successfully"));

            verify(passwordService).changePassword(eq(accountId), any());
        }
    }
    @Test
    void rootEndpoint() throws Exception {

        mockMvc.perform(get("/api/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Invalid account API endpoint"))
                .andExpect(jsonPath("$.resultInfo.resultCode")
                        .value("NOT_FOUND"));
    }
    private void setAuth() {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

}
