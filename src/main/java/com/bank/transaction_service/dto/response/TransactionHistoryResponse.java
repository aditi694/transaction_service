package com.bank.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryResponse {
    private boolean success;
    private String message;
    private String description;
    private int page;
    private int limit;
    private long total;
    private boolean hasMore;
    private List<TransactionResponse> transactions;
}