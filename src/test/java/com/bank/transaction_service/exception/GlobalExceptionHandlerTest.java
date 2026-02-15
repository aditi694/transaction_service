package com.bank.transaction_service.exception;

import com.bank.transaction_service.dto.response.BaseResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");
    }

    @Test
    void handleTransactionException_shouldReturnCustomStatus() {
        TransactionException ex =
                TransactionException.badRequest("Invalid transaction");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTransactionException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid transaction",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void handleTransactionException_shouldCover5xxBranch() {
        TransactionException ex =
                TransactionException.externalServiceError("External down");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTransactionException(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode());

        assertEquals("External down",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void handleMalformedJson_shouldReturnBadRequest() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMalformedJson(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMalformedJson_whenBodyMissing_shouldReturnSpecificMessage() {
        HttpMessageNotReadableException ex =
                mock(HttpMessageNotReadableException.class);

        when(ex.getMessage())
                .thenReturn("Required request body is missing");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMalformedJson(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals(
                "Request body is required but missing",
                response.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    void handleMalformedJson_whenMessageIsNull_shouldReturnDefaultMessage() {
        HttpMessageNotReadableException ex =
                mock(HttpMessageNotReadableException.class);

        when(ex.getMessage()).thenReturn(null);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMalformedJson(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals(
                "Invalid request format. Please check your JSON payload",
                response.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    void handleMissingParams_shouldReturnBadRequest() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("account", "String");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMissingParams(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody()
                .getResultInfo()
                .getResultMsg()
                .contains("account"));
    }

    @Test
    void handleTypeMismatch_shouldReturnBadRequest() {
        MethodArgumentTypeMismatchException ex =
                mock(MethodArgumentTypeMismatchException.class);

        when(ex.getName()).thenReturn("limit");
        doReturn(Integer.class).when(ex).getRequiredType();

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody()
                .getResultInfo()
                .getResultMsg()
                .contains("limit"));
    }

    @Test
    void handleTypeMismatch_whenRequiredTypeIsNull_shouldUseUnknown() {
        MethodArgumentTypeMismatchException ex =
                mock(MethodArgumentTypeMismatchException.class);

        when(ex.getName()).thenReturn("limit");
        doReturn(null).when(ex).getRequiredType();

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody()
                .getResultInfo()
                .getResultMsg()
                .contains("unknown"));
    }

    @Test
    void handleDateTimeParseException_shouldReturnBadRequest() {
        DateTimeParseException ex =
                new DateTimeParseException("Invalid date", "2025", 0);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleDateTimeParseException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleAuthenticationException_shouldReturnUnauthorized() {
        AuthenticationException ex =
                mock(AuthenticationException.class);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleAuthenticationException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        AccessDeniedException ex =
                new AccessDeniedException("Denied");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleFeignException_404_shouldReturnNotFound() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(404);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleFeignException_400_shouldReturnBadRequest() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(400);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleFeignException_other_shouldReturnServiceUnavailable() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(500);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleFeignException(ex, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
    }
}
