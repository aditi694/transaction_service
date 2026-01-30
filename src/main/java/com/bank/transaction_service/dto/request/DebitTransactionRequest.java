package com.bank.transaction_service.dto.request;

import com.bank.transaction_service.enums.TransactionCategory;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DebitTransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
    private TransactionCategory category;
    private String description;
    private String idempotencyKey;
}