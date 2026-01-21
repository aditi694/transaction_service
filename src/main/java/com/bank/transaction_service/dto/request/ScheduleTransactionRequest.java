package com.bank.transaction_service.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleTransactionRequest {

    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String frequency; // DAILY, WEEKLY, MONTHLY
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
