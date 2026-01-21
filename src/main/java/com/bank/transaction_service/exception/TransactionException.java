package com.bank.transaction_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransactionException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    private TransactionException(
            String message,
            String errorCode,
            HttpStatus status
    ) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    /* ================= GENERIC ================= */

    public static TransactionException badRequest(String msg) {
        return new TransactionException(
                msg,
                "BAD_REQUEST",
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException unauthorized() {
        return new TransactionException(
                "Unauthorized access",
                "UNAUTHORIZED",
                HttpStatus.UNAUTHORIZED
        );
    }

    public static TransactionException forbidden(String msg) {
        return new TransactionException(
                msg,
                "FORBIDDEN",
                HttpStatus.FORBIDDEN
        );
    }

    /* ================= TRANSACTION ================= */

    public static TransactionException insufficientBalance() {
        return new TransactionException(
                "Insufficient balance",
                "INSUFFICIENT_BALANCE",
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException transactionNotFound() {
        return new TransactionException(
                "Transaction not found",
                "TRANSACTION_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    public static TransactionException limitExceeded() {
        return new TransactionException(
                "Transaction limit exceeded",
                "LIMIT_EXCEEDED",
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException fraudDetected() {
        return new TransactionException(
                "Transaction blocked due to suspected fraud",
                "FRAUD_DETECTED",
                HttpStatus.FORBIDDEN
        );
    }
}
