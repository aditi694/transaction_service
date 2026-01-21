package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private String transactionId;
    private String transactionType;
    private String category;

    private BigDecimal amount;
    private BigDecimal charges;
    private BigDecimal totalAmount;

    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    private String status;
    private String referenceNumber;
    private LocalDateTime timestamp;
}
