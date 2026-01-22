package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.TransactionLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionLimitRepository
        extends JpaRepository<TransactionLimit, String> {
    Optional<TransactionLimit> findByAccountNumber(String accountNumber);
}
