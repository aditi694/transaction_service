package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;

public interface TransactionLimitService {

    TransactionLimitResponse update(
            String accountNumber,
            LimitUpdateRequest request);

    TransactionLimitResponse get(String accountNumber);
}
