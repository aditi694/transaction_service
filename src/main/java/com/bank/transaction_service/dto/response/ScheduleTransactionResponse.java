package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ScheduleTransactionResponse {

    private String scheduleId;
    private String accountNumber;
    private String frequency;
    private LocalDate nextExecutionDate;
    private String status;

}
