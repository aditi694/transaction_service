package com.bank.transaction_service.dto.response;

import com.bank.transaction_service.entity.TransactionLimit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
public class TransactionLimitResponse {

    private String accountNumber;
    private BigDecimal dailyLimit;
    private BigDecimal perTransactionLimit;
    private BigDecimal monthlyLimit;

    public static TransactionLimitResponse from(TransactionLimit limit) {
        return TransactionLimitResponse.builder()
                .accountNumber(limit.getAccountNumber())
                .dailyLimit(limit.getDailyLimit())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .build();
    }
}
