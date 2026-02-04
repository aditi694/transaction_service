package com.bank.transaction_service.entity;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "transaction_limits")
@Getter
@Setter
@NoArgsConstructor
public class TransactionLimit {

    @Id
    private String accountNumber;

    private BigDecimal dailyLimit = BigDecimal.valueOf(100000);
    private BigDecimal perTransactionLimit = BigDecimal.valueOf(50000);
    private BigDecimal monthlyLimit = BigDecimal.valueOf(1000000);

    private BigDecimal atmLimit = BigDecimal.valueOf(25000);
    private BigDecimal onlineShoppingLimit = BigDecimal.valueOf(30000);

    private LocalDateTime updatedAt;

    public TransactionLimit(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void update(LimitUpdateRequest req) {
        this.dailyLimit = req.getDailyLimit();
        this.perTransactionLimit = req.getPerTransactionLimit();
        this.monthlyLimit = req.getMonthlyLimit();
        this.atmLimit = req.getAtmLimit();
        this.onlineShoppingLimit = req.getOnlineShoppingLimit();
        this.updatedAt = LocalDateTime.now();
    }
}
