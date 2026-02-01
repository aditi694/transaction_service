package com.bank.transaction_service.enums;

public enum SagaStep {
    TRANSACTION_CREATED,
    DEBIT_DONE,
    CREDIT_DONE,
    COMPLETED,
    STARTED,
    DEBIT,
    CREDIT,
    COMPENSATED,
    CREDIT_SENT,
    DEBIT_SENT
}