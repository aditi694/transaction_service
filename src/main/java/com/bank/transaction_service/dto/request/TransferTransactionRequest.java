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

    private BigDecimal calculateCharges(TransferMode mode, BigDecimal amount) {
        return switch (mode) {
            case IMPS -> BigDecimal.valueOf(5);
            case NEFT -> BigDecimal.ZERO;
            case RTGS -> amount.compareTo(BigDecimal.valueOf(200000)) > 0
                    ? BigDecimal.valueOf(30)
                    : BigDecimal.valueOf(25);
            case UPI -> BigDecimal.ZERO;
        };
    }
}
