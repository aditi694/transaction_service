package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.TransactionSaga;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionSagaRepository extends JpaRepository<TransactionSaga, String> {
}