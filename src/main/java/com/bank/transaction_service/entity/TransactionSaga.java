package com.bank.transaction_service.entity;

import com.bank.transaction_service.enums.SagaStatus;
import com.bank.transaction_service.enums.SagaStep;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "transaction_saga")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSaga {

    @Id
    private String sagaId;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "from_account")
    private String fromAccount;

    @Column(name = "to_account")
    private String toAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 50, nullable = false)
    private SagaStep currentStep;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
