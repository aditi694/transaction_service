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


    public static BeneficiaryResponse from(Beneficiary entity) {

        String status =
                !entity.isActive() ? "REJECTED"
                        : entity.isVerified() ? "VERIFIED"
                        : "PENDING";

        return BeneficiaryResponse.builder()
                .beneficiaryId(entity.getBeneficiaryId())
                .beneficiaryName(entity.getBeneficiaryName())
                .beneficiaryAccount(entity.getBeneficiaryAccount())
                .ifscCode(entity.getIfscCode())
                .bankName(entity.getBankName())
                .verified(entity.isVerified())
                .active(entity.isActive())
                .verificationStatus(status)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}