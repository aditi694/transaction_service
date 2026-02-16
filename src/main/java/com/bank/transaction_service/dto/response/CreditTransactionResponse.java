package com.bank.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditTransactionResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;
}