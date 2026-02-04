package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private String transactionId;
    private String date;
    private String time;

    private String type;
    private String category;
    private String description;

    private String amount;
//    private BigDecimal balanceAfter;

    private String status;
    private String statusMessage;

}
