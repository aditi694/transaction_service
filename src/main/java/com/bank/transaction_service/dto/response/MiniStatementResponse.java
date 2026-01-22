package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MiniStatementResponse {

    private boolean success;

    private String accountNumber;
    private BigDecimal currentBalance;

    private List<MiniTxn> lastTransactions;

    @Data
    @Builder
    public static class MiniTxn {
        private String date;
        private String description;
        private BigDecimal amount;
        private BigDecimal balance;
    }
}
