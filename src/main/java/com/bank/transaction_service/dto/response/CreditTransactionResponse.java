package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class CreditTransactionResponse {
    private boolean success;
    private String message;
    private String transactionId;
//    private BigDecimal amount;
//    private BigDecimal previousBalance;
//    private BigDecimal currentBalance;
    private String status;
    private LocalDateTime timestamp;
//    private String referenceNumber;
}