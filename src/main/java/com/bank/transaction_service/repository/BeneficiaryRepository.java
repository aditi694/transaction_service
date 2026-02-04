package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {

    List<Beneficiary> findByCustomerId(String customerId);

    List<Beneficiary> findByIsVerifiedAndIsActive(boolean isVerified, boolean isActive);

    boolean existsByCustomerIdAndBeneficiaryAccount(
            String customerId,
            String beneficiaryAccount
    );
}