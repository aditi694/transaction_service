package com.bank.transaction_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LimitUpdateRequest {

    @NotNull(message = "Daily limit is required")
    private BigDecimal dailyLimit;

    @NotNull(message = "Per transaction limit is required")
    private BigDecimal perTransactionLimit;

    @NotNull(message = "Monthly limit is required")
    private BigDecimal monthlyLimit;

    @NotNull(message = "ATM limit is required")
    private BigDecimal atmLimit;

    @NotNull(message = "Online shopping limit is required")
    private BigDecimal onlineShoppingLimit;
}
