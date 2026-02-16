package com.bank.transaction_service.exception;

import com.bank.transaction_service.dto.response.BaseResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");
        return request;
    }

    @Test
    void testTransactionException() {
        TransactionException ex =
                new TransactionException("Error", "ERR001", HttpStatus.BAD_REQUEST);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTransactionException(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error", response.getBody().getResultInfo().getResultMsg());
    }
    @Test
    void testMalformedJson_MessageNull() {

        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException(null);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMalformedJson(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Invalid request format. Please check your JSON payload",
                response.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    void testMalformedJson_MissingBody() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("Required request body is missing");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMalformedJson(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body is required but missing",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testMalformedJson_InvalidFormat() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("Invalid JSON");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMalformedJson(ex, request());

        assertEquals("Invalid request format. Please check your JSON payload",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testMissingParameter() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("accountNumber", "String");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleMissingParams(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getResultInfo().getResultMsg()
                .contains("accountNumber"));
    }

    @Test
    void testTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                mock(MethodArgumentTypeMismatchException.class);

        when(ex.getName()).thenReturn("limit");
        doReturn(Integer.class).when(ex).getRequiredType();

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTypeMismatch(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getResultInfo().getResultMsg()
                .contains("limit"));
    }
    @Test
    void testTypeMismatch_WhenRequiredTypeNull() {
        MethodArgumentTypeMismatchException ex =
                mock(MethodArgumentTypeMismatchException.class);

        when(ex.getName()).thenReturn("limit");
        when(ex.getRequiredType()).thenReturn(null);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTypeMismatch(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(
                response.getBody()
                        .getResultInfo()
                        .getResultMsg()
                        .contains("unknown")
        );
    }

    @Test
    void testDateTimeParseException() {
        DateTimeParseException ex =
                new DateTimeParseException("Invalid", "2026-99-99", 0);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleDateTimeParseException(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid date format. Please use format: yyyy-MM-dd or yyyy-MM",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testAuthenticationException() {
        AuthenticationException ex =
                mock(AuthenticationException.class);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleAuthenticationException(ex, request());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed. Please login again",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testAccessDenied() {
        AccessDeniedException ex =
                new AccessDeniedException("Denied");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleAccessDenied(ex, request());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You do not have permission to perform this action",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testFeignException_404() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(404);
        when(ex.getMessage()).thenReturn("Not Found");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleFeignException(ex, request());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Requested resource not found in external service",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testFeignException_400() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(400);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleFeignException(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request to external service",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testFeignException_Default() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(500);

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleFeignException(ex, request());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("External service is temporarily unavailable. Please try again later",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testGenericException() {
        Exception ex = new RuntimeException("Error");

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleGenericException(ex, request());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later",
                response.getBody().getResultInfo().getResultMsg());
    }
    @Test
    void testTransactionException_5xx() {
        TransactionException ex =
                new TransactionException(
                        "Server Error",
                        "ERR500",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );

        ResponseEntity<BaseResponse<Void>> response =
                handler.handleTransactionException(ex, request());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());

        assertEquals("Server Error",
                response.getBody().getResultInfo().getResultMsg());
    }

}
