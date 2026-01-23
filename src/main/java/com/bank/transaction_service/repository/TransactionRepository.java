package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByAccountNumberOrderByCreatedAtDesc(
            String accountNumber, Pageable pageable);

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findTop5ByAccountNumberOrderByCreatedAtDesc(
            String accountNumber);

    List<Transaction> findByCustomerId(UUID customerId); // ✅ ADD THIS

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

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.accountNumber = :accountNumber
          AND CAST(t.createdAt AS date) = :date
    """)
    List<Transaction> findByAccountNumberAndDate(
            @Param("accountNumber") String accountNumber,
            @Param("date") LocalDate date);
}
