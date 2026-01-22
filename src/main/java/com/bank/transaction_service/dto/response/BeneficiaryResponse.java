package com.bank.transaction_service.dto.response;

import com.bank.transaction_service.entity.Beneficiary;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BeneficiaryResponse {
    private String beneficiaryId;
    private String beneficiaryName;
    private String beneficiaryAccount;
    private String ifscCode;
    private String bankName;
    private boolean verified;
    private boolean active;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    public static BeneficiaryResponse from(com.bank.transaction_service.entity.Beneficiary b) {
        return BeneficiaryResponse.builder()
                .beneficiaryId(b.getBeneficiaryId())
                .beneficiaryName(b.getBeneficiaryName())
                .beneficiaryAccount(b.getBeneficiaryAccount())
                .ifscCode(b.getIfscCode())
                .bankName(b.getBankName())
                .verified(b.isVerified())
                .active(b.isActive())
                .verificationStatus(b.isVerified() ? "VERIFIED" : "PENDING")
                .createdAt(b.getCreatedAt())
                .verifiedAt(b.getVerifiedAt())
                .build();
    }
}