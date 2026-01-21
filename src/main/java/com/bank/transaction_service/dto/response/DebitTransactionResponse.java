package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DebitTransactionResponse {
    private boolean success;
    private String transactionId;
}
