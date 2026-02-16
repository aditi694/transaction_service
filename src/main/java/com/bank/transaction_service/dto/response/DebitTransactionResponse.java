
package com.bank.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitTransactionResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private String status;
    private LocalDateTime timestamp;
//    private String referenceNumber;
}