package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryControllerTest {

    @Mock
    private BeneficiaryService beneficiaryService;

    @InjectMocks
    private BeneficiaryController beneficiaryController;

    private UUID customerId = UUID.randomUUID();

    private void mockAuthUser(boolean authenticated) {
        AuthUser mockUser = mock(AuthUser.class);
        when(mockUser.getCustomerId()).thenReturn(customerId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(authenticated);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAdd_Verified() {
        mockAuthUser(true);

        BeneficiaryRequest request = new BeneficiaryRequest();
        BeneficiaryResponse response = new BeneficiaryResponse();
        response.setVerificationStatus("VERIFIED");

        when(beneficiaryService.add(any())).thenReturn(response);

        ResponseEntity<BaseResponse<BeneficiaryResponse>> result =
                beneficiaryController.add(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(AppConstants.BENEFICIARY_VERIFIED_MSG,
                result.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testAdd_Pending() {
        mockAuthUser(true);

        BeneficiaryRequest request = new BeneficiaryRequest();
        BeneficiaryResponse response = new BeneficiaryResponse();
        response.setVerificationStatus("PENDING");

        when(beneficiaryService.add(any())).thenReturn(response);

        ResponseEntity<BaseResponse<BeneficiaryResponse>> result =
                beneficiaryController.add(request);

        Assertions.assertEquals(AppConstants.BENEFICIARY_PENDING_MSG,
                result.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testAdd_Unauthorized() {
        SecurityContextHolder.clearContext();

        BeneficiaryRequest request = new BeneficiaryRequest();
        Assertions.assertThrows(AccessDeniedException.class, () ->
                beneficiaryController.add(request)
        );
    }

    @Test
    void testListOwn_WithData() {
        mockAuthUser(true);

        when(beneficiaryService.list(any()))
                .thenReturn(List.of(new BeneficiaryResponse()));

        ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> result =
                beneficiaryController.listOwn();

        Assertions.assertEquals("Beneficiaries fetched successfully",
                result.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testListOwn_Empty() {
        mockAuthUser(true);

        when(beneficiaryService.list(any()))
                .thenReturn(List.of());

        ResponseEntity<BaseResponse<List<BeneficiaryResponse>>> result =
                beneficiaryController.listOwn();

        Assertions.assertEquals("No beneficiaries found",
                result.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testListOwn_Unauthorized() {
        SecurityContextHolder.clearContext();

        Assertions.assertThrows(AccessDeniedException.class, () ->
                beneficiaryController.listOwn()
        );
    }

    @Test
    void testUnauthorized_WhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                beneficiaryController.listOwn()
        );
    }

    @Test
    void testUnauthorized_WhenPrincipalInvalid() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("SomeString");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                beneficiaryController.listOwn()
        );
    }

}
