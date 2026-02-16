package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.util.AppConstants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {
    @Mock
    private BeneficiaryService beneficiaryService;
    @InjectMocks
    private AdminController adminController;

    private void mockSecurity(boolean isAdmin) {
        AuthUser mockUser = mock(AuthUser.class);
        when(mockUser.isAdmin()).thenReturn(isAdmin);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testListAll_Admin() {
        mockSecurity(true);
        when(beneficiaryService.listAll())
                .thenReturn(List.of(new BeneficiaryResponse()));

        ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> response =
                adminController.list(false);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("All beneficiaries fetched",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testListPending_Admin() {
        mockSecurity(true);

        when(beneficiaryService.listPendingApprovals())
                .thenReturn(List.of(new BeneficiaryResponse()));

        ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> response =
                adminController.list(true);

        Assertions.assertEquals("Pending beneficiaries fetched",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testListPending_EmptyList() {
        mockSecurity(true);

        when(beneficiaryService.listPendingApprovals())
                .thenReturn(List.of());

        ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> response =
                adminController.list(true);

        Assertions.assertEquals("No pending beneficiary approvals",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testListAll_EmptyList() {
        mockSecurity(true);

        when(beneficiaryService.listAll())
                .thenReturn(List.of());

        ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> response =
                adminController.list(false);

        Assertions.assertEquals("No beneficiaries found",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testList_NotAdmin_ShouldThrow() {
        mockSecurity(false);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                adminController.list(false)
        );
    }

    @Test
    void testApprove_Admin() {
        mockSecurity(true);

        ResponseEntity<BaseResponse<Void>> response =
                adminController.approve("123");

        verify(beneficiaryService).adminVerify("123");

        Assertions.assertEquals(AppConstants.BENEFICIARY_VERIFIED_MSG,
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testReject_Admin() {
        mockSecurity(true);

        ResponseEntity<BaseResponse<Void>> response =
                adminController.reject("123");

        verify(beneficiaryService).reject("123");

        Assertions.assertEquals(AppConstants.BENEFICIARY_REJECTED_MSG,
                response.getBody().getResultInfo().getResultMsg());
    }
}