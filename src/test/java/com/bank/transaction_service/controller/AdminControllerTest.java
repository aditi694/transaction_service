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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private BeneficiaryService beneficiaryService;

// {For AuthUser :-
// AuthUser authUser = new AuthUser(customerId, role);
//   UsernamePasswordAuthenticationToken authentication =
//      new UsernamePasswordAuthenticationToken(
//       authUser,null,authUser.getAuthorities());
//  SecurityContextHolder.getContext().setAuthentication(authentication);}

    @Test
    void listAll_success() throws Exception {
        AuthUser admin = new AuthUser(UUID.randomUUID(), "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(beneficiaryService.listAll())
                .thenReturn(List.of(
                        BeneficiaryResponse.builder()
                                .beneficiaryId("1")
                                .beneficiaryName("Test User")
                                .verified(true)
                                .active(true)
                                .build()
                ));

        mockMvc.perform(get("/api/admin/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("All beneficiaries fetched"));

        verify(beneficiaryService).listAll();
    }
    @Test
    void listPending_success() throws Exception {
        AuthUser admin = new AuthUser(UUID.randomUUID(), "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(beneficiaryService.listPendingApprovals())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/beneficiaries")
                        .param("pendingOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("No pending beneficiary approvals"));

        verify(beneficiaryService).listPendingApprovals();
    }
    @Test
    void listPending_notEmpty() throws Exception {
        AuthUser admin = new AuthUser(UUID.randomUUID(), "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(beneficiaryService.listPendingApprovals())
                .thenReturn(List.of(
                        BeneficiaryResponse.builder()
                                .beneficiaryId("1")
                                .beneficiaryName("Test")
                                .build()
                ));

        mockMvc.perform(get("/api/admin/beneficiaries")
                        .param("pendingOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Pending beneficiaries fetched"));
    }
    @Test
    void listAll_empty() throws Exception {
        AuthUser admin = new AuthUser(UUID.randomUUID(), "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        when(beneficiaryService.listAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("No beneficiaries found"));
    }

    @Test
    void approve_success() throws Exception {
        AuthUser admin = new AuthUser(UUID.randomUUID(), "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(post("/api/admin/beneficiaries/123/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value(AppConstants.BENEFICIARY_VERIFIED_MSG));

        verify(beneficiaryService).adminVerify("123");
    }

    @Test
    void reject_success() throws Exception {
        AuthUser admin = new AuthUser(UUID.randomUUID(), "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(post("/api/admin/beneficiaries/123/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value(AppConstants.BENEFICIARY_REJECTED_MSG));

        verify(beneficiaryService).reject("123");
    }

    @Test
    void list_shouldFail_whenCustomer() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/admin/beneficiaries"))
                .andExpect(status().isForbidden());
    }
}
