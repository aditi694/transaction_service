package com.bank.transaction_service.dto.response;

import com.bank.transaction_service.entity.TransactionLimit;
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
public class TransactionLimitResponse {

    private String accountNumber;

    private BigDecimal dailyLimit;
    private BigDecimal perTransactionLimit;
    private BigDecimal monthlyLimit;

    private BigDecimal atmLimit;
    private BigDecimal onlineShoppingLimit;

    private LocalDateTime updatedAt;

    public static TransactionLimitResponse from(TransactionLimit limit) {
        return TransactionLimitResponse.builder()
                .accountNumber(limit.getAccountNumber())
                .dailyLimit(limit.getDailyLimit())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .atmLimit(limit.getAtmLimit())
                .onlineShoppingLimit(limit.getOnlineShoppingLimit())
                .updatedAt(limit.getUpdatedAt())
                .build();
    }
}
