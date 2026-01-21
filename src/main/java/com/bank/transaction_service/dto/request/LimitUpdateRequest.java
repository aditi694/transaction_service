package com.bank.transaction_service.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LimitUpdateRequest {
    private BigDecimal dailyLimit;
    private BigDecimal perTransactionLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal atmLimit;
    private BigDecimal onlineShoppingLimit;
    private boolean internationalEnabled;
}
