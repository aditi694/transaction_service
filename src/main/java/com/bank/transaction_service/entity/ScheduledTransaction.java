package com.bank.transaction_service.entity;

import com.bank.transaction_service.dto.request.DebitTransactionRequest;
import com.bank.transaction_service.enums.Frequency;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.enums.TransactionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String transactionType;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduledStatus status;

    private LocalDateTime createdAt;

    private int executionCount;

    private LocalDateTime lastExecutedAt;

    private String failureReason;

    public void updateNextExecutionDate() {
        this.nextExecutionDate = frequency.next(this.nextExecutionDate);
        this.executionCount++;
        this.lastExecutedAt = LocalDateTime.now();
        this.status = ScheduledStatus.ACTIVE;
        this.failureReason = null;
    }

    public void markFailed() {
        this.status = ScheduledStatus.FAILED;
        this.failureReason = "Transaction execution failed";
    }

    public DebitTransactionRequest toDebitRequest() {
        return DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(amount)
                .category(TransactionCategory.valueOf(transactionType))
                .description(description != null
                        ? description
                        : "Scheduled " + frequency + " transaction")
                .build();
    }
}