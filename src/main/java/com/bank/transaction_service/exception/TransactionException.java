package com.bank.transaction_service.exception;

import com.bank.transaction_service.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransactionException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    private TransactionException(String message, ErrorCode errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    // 400 BAD REQUEST
    public static TransactionException badRequest(String message) {
        return new TransactionException(message, ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public static TransactionException invalidAmount() {
        return new TransactionException(
                "Transaction amount must be greater than zero",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException invalidAccountNumber() {
        return new TransactionException(
                "Invalid account number format",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException sameAccountTransfer() {
        return new TransactionException(
                "Cannot transfer to the same account",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException invalidTransferMode() {
        return new TransactionException(
                "Invalid transfer mode. Allowed: NEFT, RTGS, IMPS, UPI",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException invalidBeneficiary() {
        return new TransactionException(
                "Beneficiary is not verified or does not exist",
                ErrorCode.INVALID_BENEFICIARY,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException beneficiaryAlreadyExists() {
        return new TransactionException(
                "This beneficiary account is already added",
                ErrorCode.INVALID_BENEFICIARY,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException beneficiaryNameMismatch() {
        return new TransactionException(
                "Beneficiary name does not match account holder name",
                ErrorCode.INVALID_BENEFICIARY,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException invalidIfsc() {
        return new TransactionException(
                "Invalid IFSC code format",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException limitExceeded(String message) {
        return new TransactionException(
                message,
                ErrorCode.LIMIT_EXCEEDED,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException dailyLimitExceeded(String used, String limit) {
        return new TransactionException(
                String.format("Daily transaction limit exceeded. Used: ₹%s, Limit: ₹%s", used, limit),
                ErrorCode.LIMIT_EXCEEDED,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException perTransactionLimitExceeded(String limit) {
        return new TransactionException(
                String.format("Per transaction limit of ₹%s exceeded", limit),
                ErrorCode.LIMIT_EXCEEDED,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException monthlyLimitExceeded() {
        return new TransactionException(
                "Monthly transaction limit exceeded",
                ErrorCode.LIMIT_EXCEEDED,
                HttpStatus.BAD_REQUEST
        );
    }

    public static TransactionException atmLimitExceeded() {
        return new TransactionException(
                "ATM withdrawal limit exceeded for today",
                ErrorCode.LIMIT_EXCEEDED,
                HttpStatus.BAD_REQUEST
        );
    }

    // 401 UNAUTHORIZED
    public static TransactionException unauthorized(String message) {
        return new TransactionException(
                message,
                ErrorCode.UNAUTHORIZED_ACCESS,
                HttpStatus.UNAUTHORIZED
        );
    }

    public static TransactionException invalidToken() {
        return new TransactionException(
                "Invalid or expired authentication token",
                ErrorCode.UNAUTHORIZED_ACCESS,
                HttpStatus.UNAUTHORIZED
        );
    }

    public static TransactionException accountOwnershipRequired() {
        return new TransactionException(
                "You can only perform transactions on your own accounts",
                ErrorCode.UNAUTHORIZED_ACCESS,
                HttpStatus.UNAUTHORIZED
        );
    }

    // 404 NOT FOUND
    public static TransactionException transactionNotFound() {
        return new TransactionException(
                "Transaction not found with the provided ID",
                ErrorCode.TRANSACTION_NOT_FOUND,
                HttpStatus.NOT_FOUND
        );
    }

    public static TransactionException accountNotFound() {
        return new TransactionException(
                "Account not found or does not exist",
                ErrorCode.ACCOUNT_NOT_FOUND,
                HttpStatus.NOT_FOUND
        );
    }

    public static TransactionException beneficiaryNotFound() {
        return new TransactionException(
                "Beneficiary not found",
                ErrorCode.INVALID_BENEFICIARY,
                HttpStatus.NOT_FOUND
        );
    }

    public static TransactionException scheduledTransactionNotFound() {
        return new TransactionException(
                "Scheduled transaction not found",
                ErrorCode.TRANSACTION_NOT_FOUND,
                HttpStatus.NOT_FOUND
        );
    }

    // 409 CONFLICT
    public static TransactionException insufficientBalance() {
        return new TransactionException(
                "Insufficient balance in your account to complete this transaction",
                ErrorCode.INSUFFICIENT_BALANCE,
                HttpStatus.CONFLICT
        );
    }

    public static TransactionException insufficientBalanceWithDetails(String available, String required) {
        return new TransactionException(
                String.format("Insufficient balance. Available: ₹%s, Required: ₹%s", available, required),
                ErrorCode.INSUFFICIENT_BALANCE,
                HttpStatus.CONFLICT
        );
    }

    public static TransactionException duplicateTransaction() {
        return new TransactionException(
                "Duplicate transaction detected. This transaction was already processed",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.CONFLICT
        );
    }

    public static TransactionException transactionInProgress() {
        return new TransactionException(
                "A similar transaction is already in progress",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.CONFLICT
        );
    }

    public static TransactionException scheduledTransactionNotActive() {
        return new TransactionException(
                "Scheduled transaction is not in active status",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.CONFLICT
        );
    }

    // 422 UNPROCESSABLE ENTITY
    public static TransactionException unprocessableEntity(String message) {
        return new TransactionException(
                message,
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public static TransactionException transactionFailed(String reason) {
        return new TransactionException(
                String.format("Transaction failed: %s", reason),
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public static TransactionException accountBlocked() {
        return new TransactionException(
                "Cannot process transaction. Account is blocked",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public static TransactionException accountInactive() {
        return new TransactionException(
                "Cannot process transaction. Account is inactive",
                ErrorCode.INVALID_TRANSACTION,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    public static TransactionException beneficiaryNotVerified() {
        return new TransactionException(
                "Beneficiary is not yet verified. Please wait for admin approval",
                ErrorCode.INVALID_BENEFICIARY,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    // 503 SERVICE UNAVAILABLE
    public static TransactionException externalServiceError(String message) {
        return new TransactionException(
                message,
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static TransactionException accountServiceUnavailable() {
        return new TransactionException(
                "Account service is temporarily unavailable. Please try again later",
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static TransactionException customerServiceUnavailable() {
        return new TransactionException(
                "Customer service is temporarily unavailable. Please try again later",
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    public static TransactionException paymentGatewayError() {
        return new TransactionException(
                "Payment gateway is experiencing issues. Please try again",
                ErrorCode.EXTERNAL_SERVICE_ERROR,
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}