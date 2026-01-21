package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, String> {

    List<Beneficiary> findByCustomerId(String customerId);
}
