package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryService {

    // Customer operations
    BeneficiaryResponse add(BeneficiaryRequest request);

    List<BeneficiaryResponse> list(String customerId);

    BeneficiaryResponse get(String beneficiaryId);

    // Admin operations
    void adminVerify(String beneficiaryId, UUID adminId);

    void reject(String beneficiaryId);

    List<BeneficiaryResponse> listAll();

    List<BeneficiaryResponse> listPendingApprovals();
}