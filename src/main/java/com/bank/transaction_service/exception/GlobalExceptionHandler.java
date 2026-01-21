package com.bank.transaction_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleTransaction(
            TransactionException ex
    ) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ApiErrorResponse(
                        ex.getMessage(),
                        ex.getErrorCode(),
                        ex.getStatus().value(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex
    ) {
        return ResponseEntity
                .status(500)
                .body(new ApiErrorResponse(
                        "Internal server error",
                        "INTERNAL_SERVER_ERROR",
                        500,
                        LocalDateTime.now()
                ));
    }
}
