package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.Transaction;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByAccountNumberOrderByCreatedAtDesc(
            String accountNumber, Pageable pageable);

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findTop5ByAccountNumberOrderByCreatedAtDesc(
            String accountNumber);

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.accountNumber = :accountNumber
          AND MONTH(t.createdAt) = :month
          AND YEAR(t.createdAt) = :year
    """)
    List<Transaction> findByAccountNumberAndMonth(
            @Param("accountNumber") String accountNumber,
            @Param("month") int month,
            @Param("year") int year);
}
