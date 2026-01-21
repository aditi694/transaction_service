package com.bank.transaction_service.dto.response;

import com.bank.transaction_service.entity.Beneficiary;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaryResponse {

    private String beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private boolean verified;

    public static BeneficiaryResponse from(Beneficiary b) {
        return BeneficiaryResponse.builder()
                .beneficiaryId(b.getBeneficiaryId())
                .beneficiaryName(b.getBeneficiaryName())
                .beneficiaryAccount(b.getBeneficiaryAccount())
                .verified(b.isVerified())
                .build();
    }
}
