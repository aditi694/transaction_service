package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {

    BeneficiaryResponse add(BeneficiaryRequest request);

    List<BeneficiaryResponse> list(String customerId);

    void verify(String beneficiaryId);

    BeneficiaryResponse get(String beneficiaryId); // 🆕 Added
}