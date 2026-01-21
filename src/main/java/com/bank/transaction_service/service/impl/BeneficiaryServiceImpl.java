package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.entity.Beneficiary;
import com.bank.transaction_service.repository.BeneficiaryRepository;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.validation.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository repository;

    @Override
    public BeneficiaryResponse add(BeneficiaryRequest req) {

        TransactionValidator.validateBeneficiary(req);

        Beneficiary entity = Beneficiary.builder()
                .beneficiaryId("BEN-" + System.currentTimeMillis())
                .customerId(String.valueOf(req.getCustomerId()))
                .accountNumber(req.getAccountNumber())
                .beneficiaryName(req.getBeneficiaryName())
                .beneficiaryAccount(req.getBeneficiaryAccount())
                .ifscCode(req.getIfscCode())
                .verified(false)
                .build();

        repository.save(entity);

        return BeneficiaryResponse.builder()
                .beneficiaryId(entity.getBeneficiaryId())
                .beneficiaryName(entity.getBeneficiaryName())
                .beneficiaryAccount(entity.getBeneficiaryAccount())
                .verified(entity.isVerified())
                .build();

        @Override
    public List<BeneficiaryResponse> list(UUID customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    @Override
    public void verify(String beneficiaryId) {
        Beneficiary b =
                repository.findById(beneficiaryId)
                        .orElseThrow();

        b.setVerified(true);
    }
}
