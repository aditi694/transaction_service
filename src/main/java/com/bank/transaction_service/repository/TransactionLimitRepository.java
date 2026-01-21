package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionLimitRepository
        extends JpaRepository<TransactionLimit, String> {
}
