package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import com.bank.transaction_service.service.TransactionLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class TransactionLimitServiceImpl implements TransactionLimitService {

    private final TransactionLimitRepository repository;

    @Override
    public TransactionLimitResponse get(String accountNumber) {

        TransactionLimit limit = repository.findByAccountNumber(accountNumber)
                .orElseGet(() -> repository.save(new TransactionLimit(accountNumber)));

        return TransactionLimitResponse.from(limit);
    }

    @Override
    public TransactionLimitResponse update(
            String accountNumber,
            LimitUpdateRequest request
    ) {

        TransactionLimit limit = repository.findByAccountNumber(accountNumber)
                .orElseGet(() -> repository.save(new TransactionLimit(accountNumber)));

        limit.update(request);
        repository.save(limit);

        return TransactionLimitResponse.from(limit);
    }
}
