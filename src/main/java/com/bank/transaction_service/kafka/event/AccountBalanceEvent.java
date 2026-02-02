package com.bank.transaction_service.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountBalanceEvent(
        int eventVersion,
        String eventId,
        String transactionId,
        String step,            // DEBIT | CREDIT | COMPENSATE_DEBIT
        String status,          // SUCCESS | FAILED
        String accountNumber,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime timestamp
) {}