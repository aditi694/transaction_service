package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferTransactionResponse {
    private boolean success;
    private String transactionId;
    private String utrNumber;
}
