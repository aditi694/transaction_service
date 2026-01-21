package com.bank.transaction_service.exception;

import lombok.Getter;

@Getter
public class TransactionException extends RuntimeException {

    private final String errorCode;

    public TransactionException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    // Factory methods for common exceptions

    public static TransactionException insufficientBalance() {
        return new TransactionException(
                "Insufficient balance in account",
                "TXN_001"
        );
    }

    public static TransactionException badRequest(String message) {
        return new TransactionException(message, "TXN_002");
    }

    public static TransactionException transactionNotFound() {
        return new TransactionException(
                "Transaction not found",
                "TXN_003"
        );
    }

    public static TransactionException limitExceeded(String message) {
        return new TransactionException(message, "TXN_004");
    }

    public static TransactionException unauthorized(String message) {
        return new TransactionException(message, "TXN_005");
    }

    public static TransactionException externalServiceError(String message) {
        return new TransactionException(message, "TXN_006");
    }

    public static TransactionException accountNotFound() {
        return new TransactionException(
                "Account not found",
                "TXN_007"
        );
    }

    public static TransactionException invalidBeneficiary() {
        return new TransactionException(
                "Invalid or unverified beneficiary",
                "TXN_008"
        );
    }
}