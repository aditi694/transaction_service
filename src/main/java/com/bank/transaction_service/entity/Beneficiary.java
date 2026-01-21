package com.bank.transaction_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {

    @Id
    private String beneficiaryId;

    private String customerId;
    private String accountNumber;

    private String beneficiaryName;
    private String beneficiaryAccount;
    private String ifscCode;
    private String bankName;
    private String branchName;

    private boolean isVerified;
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
