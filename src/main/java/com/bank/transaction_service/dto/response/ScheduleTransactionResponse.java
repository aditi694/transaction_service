package com.bank.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleTransactionResponse {

    private String scheduleId;
    private String accountNumber;
    private String frequency;
    private LocalDate nextExecutionDate;
    private String status;

}
