package com.bank.transaction_service.kafka.event;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionStatusEvent(
        String transactionId,
        String transactionType,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        String finalStatus,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        String email,
        UUID userId,
        String eventType
) {}