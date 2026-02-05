package com.bank.transaction_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleTransactionRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @NotBlank(message = "Frequency is required")
    private String frequency; // DAILY, WEEKLY, MONTHLY

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "Description is required")
    private String description;
}
