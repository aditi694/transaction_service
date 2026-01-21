package com.bank.transaction_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaryResponse {

    private String beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private boolean verified;
}
