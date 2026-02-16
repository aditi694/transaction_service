package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.MiniStatementResponse;
import com.bank.transaction_service.dto.response.TransactionHistoryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.TransactionQueryService;
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
public class TransactionQueryControllerTest {

    @Mock
    private TransactionQueryService queryService;

    @InjectMocks
    private TransactionQueryController controller;

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
    public void testHistory() {
        mockValidAuth();
        TransactionHistoryResponse response =
                new TransactionHistoryResponse();

        when(queryService.getHistory("ACC123", 20, 1))
                .thenReturn(response);

        ResponseEntity<BaseResponse<TransactionHistoryResponse>> result =
                controller.history("ACC123", 20, 1);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(
                "Transaction history fetched successfully",
                result.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    public void testMiniStatement() {
        mockValidAuth();
        MiniStatementResponse response =
                new MiniStatementResponse();

        when(queryService.miniStatement("ACC123"))
                .thenReturn(response);

        ResponseEntity<BaseResponse<MiniStatementResponse>> result =
                controller.miniStatement("ACC123");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(
                "Mini statement generated successfully",
                result.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    public void testUnauthorized_WhenAuthNull() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class, () ->
                controller.history("ACC123", 20, 1)
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
                controller.history("ACC123", 20, 1)
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
                controller.history("ACC123", 20, 1)
        );
    }
}
