package com.bank.transaction_service.kafka.event;

import java.math.BigDecimal;

public record TransactionCommandEvent(
        int eventVersion,
        String eventId,
        String transactionId,
        String step,        // DEBIT | CREDIT | COMPENSATE
        String fromAccount,
        String toAccount,
        BigDecimal amount
) {}