package com.bank.transaction_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class BeneficiaryRequest {

    private UUID customerId;
    private String accountNumber;          // Customer account
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String ifscCode;
    private String branchName;
}
