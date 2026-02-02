package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionStatusResponse {

    private String transactionId;
    private String status;

    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal currentBalance;

//    private String referenceNumber;
    private String failureReason;

    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}