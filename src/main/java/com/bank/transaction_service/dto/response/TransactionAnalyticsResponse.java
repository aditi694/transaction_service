package com.bank.transaction_service.dto.response;

import com.bank.transaction_service.enums.TransactionCategory;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class TransactionAnalyticsResponse {
    private String month;
    private BigDecimal totalSpent;
    private Map<TransactionCategory, BigDecimal> categoryBreakdown;


    @Data
    @Builder
    public static class MerchantSpend {
        private String name;
        private BigDecimal amount;
    }
}
