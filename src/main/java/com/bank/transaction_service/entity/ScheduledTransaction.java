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

    private String accountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String description;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;

    @Enumerated(EnumType.STRING)
    private ScheduledStatus status;

    private LocalDateTime createdAt;

    public void updateNextExecutionDate() {
        this.nextExecutionDate = frequency.next(this.nextExecutionDate);
    }

    public void markFailed() {
        this.status = ScheduledStatus.FAILED;
    }

    public DebitTransactionRequest toDebitRequest() {
        return DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(amount)
                .category(TransactionCategory.valueOf(transactionType))
                .description(description)
                .build();
    }
}
