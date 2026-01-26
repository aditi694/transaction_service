package com.bank.transaction_service.enums;

public enum ScheduledStatus {
    ACTIVE,      // Schedule is active and running
    PAUSED,      // Temporarily paused by user
    COMPLETED,   // Reached end date
    FAILED,      // Failed execution
    CANCELLED    // Cancelled by user
}