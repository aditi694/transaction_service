package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionLimitService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionLimitControllerTest {

    @Mock
    private TransactionLimitService limitService;

    @InjectMocks
    private TransactionLimitController controller;

    private void mockValidAuth() {
        AuthUser user = mock(AuthUser.class);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetLimits() {
        mockValidAuth();
        TransactionLimitResponse response = new TransactionLimitResponse();

        when(limitService.get("ACC123")).thenReturn(response);

        ResponseEntity<BaseResponse<TransactionLimitResponse>> result =
                controller.getLimits("ACC123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(
                "Transaction limits fetched successfully",
                result.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    public void testUpdateLimits() {
        mockValidAuth();
        LimitUpdateRequest request = new LimitUpdateRequest();
        TransactionLimitResponse response = new TransactionLimitResponse();

        when(limitService.update(eq("ACC123"), any()))
                .thenReturn(response);

        ResponseEntity<BaseResponse<TransactionLimitResponse>> result =
                controller.updateLimits("ACC123", request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(
                "Transaction limits updated successfully",
                result.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    public void testUnauthorized_WhenAuthNull() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                controller.getLimits("ACC123")
        );
    }

    @Test
    public void testUnauthorized_WhenNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                controller.getLimits("ACC123")
        );
    }

    @Test
    public void testUnauthorized_WhenPrincipalInvalid() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("invalid");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                controller.getLimits("ACC123")
        );
    }
}
