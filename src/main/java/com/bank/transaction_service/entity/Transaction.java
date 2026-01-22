package com.bank.transaction_service.entity;

import com.bank.transaction_service.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_account_number", columnList = "accountNumber"),
        @Index(name = "idx_customer_id", columnList = "customerId"),
        @Index(name = "idx_idempotency_key", columnList = "idempotencyKey"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // 🔥 THIS WAS MISSING
//    private Long id;
    @Id
    @Column(name = "transaction_id", length = 30)
    private String transactionId;

    @Column(nullable = false)
    private String accountNumber;

    // 🆕 Customer ID for linking to customer
    @Column(nullable = false)
    private UUID customerId;

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

    // 🆕 Idempotency key for duplicate detection
    @Column(unique = true, length = 100)
    private String idempotencyKey;

    // Metadata
    private LocalDateTime createdAt;
    private String ipAddress;
    private String device;
}