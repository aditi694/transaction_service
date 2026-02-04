package com.bank.transaction_service.dto.request;

import com.bank.transaction_service.enums.TransferMode;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TransferTransactionRequest {
    @NotNull(message = "")
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String transferType;
    private String description;
}
