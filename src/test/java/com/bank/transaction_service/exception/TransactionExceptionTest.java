package com.bank.transaction_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionExceptionTest {

    @Test
    void testConstructor() {
        TransactionException ex =
                new TransactionException("Error occurred", "ERR001", HttpStatus.BAD_REQUEST);

        assertEquals("Error occurred", ex.getMessage());
        assertEquals("ERR001", ex.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void testBadRequest() {
        TransactionException ex =
                TransactionException.badRequest("Invalid request");

        assertEquals("Invalid request", ex.getMessage());
        assertEquals("BAD_REQUEST", ex.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    void testUnauthorized() {
        TransactionException ex =
                TransactionException.unauthorized("Unauthorized access");

        assertEquals("Unauthorized access", ex.getMessage());
        assertEquals("UNAUTHORIZED", ex.getErrorCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
    }

    @Test
    void testNotFound() {
        TransactionException ex =
                TransactionException.notFound("Resource not found");

        assertEquals("Resource not found", ex.getMessage());
        assertEquals("NOT_FOUND", ex.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    void testLimitExceeded() {
        TransactionException ex =
                TransactionException.limitExceeded("Limit exceeded");

        assertEquals("Limit exceeded", ex.getMessage());
        assertEquals("LIMIT_EXCEEDED", ex.getErrorCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
    }

    @Test
    void testExternalServiceError() {
        TransactionException ex =
                TransactionException.externalServiceError("Service down");

        assertEquals("Service down", ex.getMessage());
        assertEquals("EXTERNAL_SERVICE_ERROR", ex.getErrorCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getHttpStatus());
    }
}
