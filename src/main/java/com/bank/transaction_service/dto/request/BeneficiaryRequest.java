package com.bank.transaction_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class BeneficiaryRequest {

    private String customerId;   // ✅ STRING
    private String accountNumber;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String ifscCode;
}
