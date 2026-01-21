package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.entity.Beneficiary;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.BeneficiaryRepository;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository repository;

    @Override
    public BeneficiaryResponse add(BeneficiaryRequest req) {

        TransactionValidator.validateBeneficiary(req);

        Beneficiary entity = Beneficiary.builder()
                .beneficiaryId("BEN-" + System.currentTimeMillis())
                .customerId(req.getCustomerId())
                .accountNumber(req.getAccountNumber())
                .beneficiaryName(req.getBeneficiaryName())
                .beneficiaryAccount(req.getBeneficiaryAccount())
                .ifscCode(req.getIfscCode())
                .isVerified(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        repository.save(entity);

        return BeneficiaryResponse.from(entity);
    }

    @Override
    public List<BeneficiaryResponse> list(String customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    @Override
    public void verify(String beneficiaryId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() -> TransactionException.badRequest("Beneficiary not found"));

        b.setVerified(true);
        b.setUpdatedAt(LocalDateTime.now());
        repository.save(b);
    }

    @Override
    public BeneficiaryResponse get(String beneficiaryId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() -> TransactionException.badRequest("Beneficiary not found"));

        return BeneficiaryResponse.from(b);
    }
}