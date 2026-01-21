package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryService {

    BeneficiaryResponse add(BeneficiaryRequest request);

    List<BeneficiaryResponse> list(UUID customerId);

    void verify(String beneficiaryId);
}
