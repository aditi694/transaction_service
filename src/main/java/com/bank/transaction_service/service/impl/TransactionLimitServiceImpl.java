package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import com.bank.transaction_service.service.TransactionLimitService;
import com.bank.transaction_service.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionLimitServiceImpl
        implements TransactionLimitService {

    private final TransactionLimitRepository repository;

    @Override
    public TransactionLimitResponse update(
            String accountNumber,
            LimitUpdateRequest req) {

        TransactionValidator.validateLimits(req);

        TransactionLimit limit =
                repository.findById(accountNumber)
                        .orElse(new TransactionLimit(accountNumber));

        limit.update(req);
        repository.save(limit);

        return TransactionLimitResponse.from(limit);
    }

    @Override
    public TransactionLimitResponse get(String accountNumber) {
        return TransactionLimitResponse.from(
                repository.findById(accountNumber).orElseThrow()
        );
    }
}
