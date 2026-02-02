package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.TransactionSaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionSagaRepository extends JpaRepository<TransactionSaga, String> {

    Optional<TransactionSaga> findByTransactionId(String transactionId);
}