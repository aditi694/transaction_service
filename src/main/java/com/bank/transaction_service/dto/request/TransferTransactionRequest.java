package com.bank.transaction_service.dto.request;

import com.bank.transaction_service.enums.TransferMode;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransferTransactionRequest {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String transferType;
    private String description;


}
