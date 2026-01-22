package com.bank.transaction_service.exception;

import com.bank.transaction_service.enums.ErrorCode;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(
            TransactionException ex
    ) {
        log.error("Transaction error [{}]: {}", ex.getErrorCode(), ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case UNAUTHORIZED_ACCESS -> HttpStatus.UNAUTHORIZED;
            case TRANSACTION_NOT_FOUND, ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INSUFFICIENT_BALANCE, LIMIT_EXCEEDED, INVALID_BENEFICIARY ->
                    HttpStatus.BAD_REQUEST;
            case EXTERNAL_SERVICE_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .errorCode(ex.getErrorCode().name())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        log.error("Feign error", ex);

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ErrorResponse.builder()
                                .message("External service unavailable")
                                .errorCode(ErrorCode.EXTERNAL_SERVICE_ERROR.name())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ErrorResponse.builder()
                                .message("Internal server error")
                                .errorCode(ErrorCode.INTERNAL_ERROR.name())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }
}
