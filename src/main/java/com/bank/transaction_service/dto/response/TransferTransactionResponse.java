package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferTransactionResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String transferMode;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private BigDecimal charges;
    private BigDecimal totalDeducted;
    private BigDecimal senderBalanceBefore;
    private BigDecimal senderBalanceAfter;
    private String status;
    private LocalDateTime timestamp;
    private String utrNumber;
}