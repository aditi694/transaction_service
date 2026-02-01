package com.bank.transaction_service.kafka.event;

import java.time.LocalDateTime;

public record TransactionStatusEvent(
        int eventVersion,
        String transactionId,
        String finalStatus,
        String failureReason,
        LocalDateTime completedAt

) {}