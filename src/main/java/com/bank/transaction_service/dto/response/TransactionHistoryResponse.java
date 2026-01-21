package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TransactionHistoryResponse {

    private boolean success;
    private int page;
    private int limit;
    private long total;

    private List<TransactionResponse> transactions;
}
