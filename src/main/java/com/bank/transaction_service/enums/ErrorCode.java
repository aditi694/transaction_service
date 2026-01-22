package com.bank.transaction_service.enums;

public enum ErrorCode {

    // Authentication / Authorization
    UNAUTHORIZED_ACCESS,

    // Transaction
    INSUFFICIENT_BALANCE,
    TRANSACTION_NOT_FOUND,
    INVALID_TRANSACTION,
    LIMIT_EXCEEDED,

    // Account / Beneficiary
    ACCOUNT_NOT_FOUND,
    INVALID_BENEFICIARY,

    // External services
    EXTERNAL_SERVICE_ERROR,

    // Generic
    BAD_REQUEST,
    INTERNAL_ERROR
}
