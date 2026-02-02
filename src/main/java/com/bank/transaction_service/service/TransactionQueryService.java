package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.response.*;

public interface TransactionQueryService {

    TransactionHistoryResponse getHistory(
            String accountNumber, int limit, int page);
    MiniStatementResponse miniStatement(String accountNumber);
}
