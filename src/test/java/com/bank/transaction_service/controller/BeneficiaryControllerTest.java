package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.util.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BeneficiaryController.class)
class BeneficiaryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;
    @MockBean
    private BeneficiaryService beneficiaryService;

    @Test
    void add_beneficiary() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String json = """
                {
                      "accountNumber": "999999",
                      "customerId": "dummy",
                      "beneficiaryAccount": "123456",
                      "ifscCode": "SBIN0001",
                      "beneficiaryName": "John"
                    }
                """;

        BeneficiaryResponse response = BeneficiaryResponse.builder()
                .verificationStatus("VERIFIED")
                .build();

        when(beneficiaryService.add(any()))
                .thenReturn(response);
        mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value(AppConstants.BENEFICIARY_VERIFIED_MSG));
        verify(beneficiaryService).add(any());
    }

    @Test
    void add_pendingBeneficiary() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        String json = """
                {
                      "accountNumber": "999999",
                      "customerId": "dummy",
                      "beneficiaryAccount": "123456",
                      "ifscCode": "SBIN0001",
                      "beneficiaryName": "John"
                    }
                """;

        BeneficiaryResponse response = BeneficiaryResponse.builder()
                .verificationStatus("PENDING")
                .build();

        when(beneficiaryService.add(any()))
                .thenReturn(response);
        mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value(AppConstants.BENEFICIARY_PENDING_MSG));
    }

    @Test
    void list_beneficiaries_success() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(beneficiaryService.list(any()))
                .thenReturn(List.of(BeneficiaryResponse.builder().build()));

        mockMvc.perform(get("/api/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Beneficiaries fetched successfully"));

        verify(beneficiaryService).list(any());
    }

    @Test
    void list_beneficiaries_empty() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(beneficiaryService.list(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("No beneficiaries found"));
    }

    @Test
    void add_beneficiary_wrongPrincipal() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "someStringUser", null);

        SecurityContextHolder.getContext().setAuthentication(auth);

        String validJson = """
                {
                  "accountNumber": "999999",
                  "customerId": "dummy",
                  "beneficiaryAccount": "123456",
                  "ifscCode": "SBIN0001",
                  "beneficiaryName": "John"
                }
                """;

        mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void add_beneficiary_wrongPrincipal_authenticated() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "someStringUser",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        String json = """
                {
                  "accountNumber": "999999",
                  "customerId": "dummy",
                  "beneficiaryAccount": "123456",
                  "ifscCode": "SBIN0001",
                  "beneficiaryName": "John"
                }
                """;

        mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void add_beneficiary_noAuthentication() throws Exception {
        SecurityContextHolder.clearContext();

        String json = """
                {
                  "accountNumber": "999999",
                  "customerId": "dummy",
                  "beneficiaryAccount": "123456",
                  "ifscCode": "SBIN0001",
                  "beneficiaryName": "John"
                }
                """;

        mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void add_beneficiary_notAuthenticated() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        auth.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(auth);

        String json = """
                {
                  "accountNumber": "999999",
                  "customerId": "dummy",
                  "beneficiaryAccount": "123456",
                  "ifscCode": "SBIN0001",
                  "beneficiaryName": "John"
                }
                """;

        mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

}