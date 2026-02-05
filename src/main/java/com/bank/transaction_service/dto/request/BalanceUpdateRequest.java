package com.bank.transaction_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceUpdateRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Balance delta is required")
    private BigDecimal delta;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}
