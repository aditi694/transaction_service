package com.bank.transaction_service.dto.response;

import com.bank.transaction_service.entity.TransactionLimit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionLimitResponse {

    private String accountNumber;

    private BigDecimal dailyLimit;
    private BigDecimal perTransactionLimit;
    private BigDecimal monthlyLimit;

    private BigDecimal atmLimit;
    private BigDecimal onlineShoppingLimit;

//    private boolean internationalEnabled;
//    private boolean contactlessEnabled;

    private LocalDateTime updatedAt;

    public static TransactionLimitResponse from(TransactionLimit limit) {
        return TransactionLimitResponse.builder()
                .accountNumber(limit.getAccountNumber())
                .dailyLimit(limit.getDailyLimit())
                .perTransactionLimit(limit.getPerTransactionLimit())
                .monthlyLimit(limit.getMonthlyLimit())
                .atmLimit(limit.getAtmLimit())
                .onlineShoppingLimit(limit.getOnlineShoppingLimit())
//                .internationalEnabled(limit.isInternationalEnabled())
//                .contactlessEnabled(limit.isContactlessEnabled())
                .updatedAt(limit.getUpdatedAt())
                .build();
    }
}
