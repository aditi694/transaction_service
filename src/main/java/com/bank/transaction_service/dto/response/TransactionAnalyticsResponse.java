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

    private boolean success;
    private String accountNumber;
    private String month;

    private Summary summary;
    private List<CategorySpend> categoryBreakdown;

    @Data
    @Builder
    public static class Summary {
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
        private BigDecimal netFlow;
        private long transactionCount;
    }

    @Data
    @Builder
    public static class CategorySpend {
        private String category;
        private BigDecimal amount;
    }
}
