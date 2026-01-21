package com.bank.transaction_service.entity;

import com.bank.transaction_service.enums.*;
import com.bank.transaction_service.enums.TransactionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 30)
    private String transactionId;

    @Column(nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;

    private BigDecimal amount;
    private BigDecimal charges;
    private BigDecimal totalAmount;

    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    // Transfer fields
    private String toAccount;
    private String beneficiaryName;

    @Enumerated(EnumType.STRING)
    private TransferMode transferMode;

    private String utrNumber;
    private String ifscCode;

    // Metadata
    private LocalDateTime createdAt;
    private String ipAddress;
    private String device;
}
