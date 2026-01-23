package com.bank.transaction_service.exception;

import com.bank.transaction_service.enums.ErrorCode;
import lombok.Getter;

@Getter
public class TransactionException extends RuntimeException {

    private final ErrorCode errorCode;

    private TransactionException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public static TransactionException insufficientBalance() {
        return new TransactionException(
                "Insufficient balance in account",
                ErrorCode.INSUFFICIENT_BALANCE
        );
    }

    public static TransactionException transactionNotFound() {
        return new TransactionException(
                "Transaction not found",
                ErrorCode.TRANSACTION_NOT_FOUND
        );
    }

    public static TransactionException unauthorized(String message) {
        return new TransactionException(
                message,
                ErrorCode.UNAUTHORIZED_ACCESS
        );
    }

    public static TransactionException limitExceeded(String message) {
        return new TransactionException(
                message,
                ErrorCode.LIMIT_EXCEEDED
        );
    }

    public static TransactionException badRequest(String message) {
        return new TransactionException(
                message,
                ErrorCode.BAD_REQUEST
        );
    }

    public static TransactionException accountNotFound() {
        return new TransactionException(
                "Account not found",
                ErrorCode.ACCOUNT_NOT_FOUND
        );
    }

    public static TransactionException invalidBeneficiary() {
        return new TransactionException(
                "Invalid or unverified beneficiary",
                ErrorCode.INVALID_BENEFICIARY
        );
    }

    public static TransactionException externalServiceError(String message) {
        return new TransactionException(
                message,
                ErrorCode.EXTERNAL_SERVICE_ERROR
        );
    }
}
