package com.bank.transaction_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransactionException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public TransactionException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static TransactionException badRequest(String message) {
        return new TransactionException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public static TransactionException unauthorized(String message) {
        return new TransactionException(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    public static TransactionException notFound(String message) {
        return new TransactionException(message, "NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    public static TransactionException limitExceeded(String message) {
        return new TransactionException(message, "LIMIT_EXCEEDED", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static TransactionException externalServiceError(String message) {
        return new TransactionException(message, "EXTERNAL_SERVICE_ERROR", HttpStatus.SERVICE_UNAVAILABLE);
    }
}