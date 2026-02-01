package com.bank.transaction_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateRequest {

    private String accountNumber;
    private BigDecimal delta;
    private String transactionId;
}