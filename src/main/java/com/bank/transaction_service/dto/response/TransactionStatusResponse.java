package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionStatusResponse {

    private String transactionId;
    private String status;
    private String message;
    private LocalDateTime completedAt;
}
