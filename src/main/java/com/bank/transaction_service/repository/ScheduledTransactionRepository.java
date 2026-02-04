package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.ScheduledStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, UUID> {

    @Query("""
        SELECT s FROM ScheduledTransaction s
        WHERE s.status = 'ACTIVE'
          AND s.nextExecutionDate <= :today
    """)
    List<ScheduledTransaction> findDueTransactions(@Param("today") LocalDate today);

    List<ScheduledTransaction> findByAccountNumber(String accountNumber);
}