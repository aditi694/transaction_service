package com.bank.transaction_service.validation;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.exception.TransactionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionValidatorTest {

    @Test
    void validateBeneficiary_validRequest_shouldPass() {
        BeneficiaryRequest req = new BeneficiaryRequest();
        req.setBeneficiaryAccount("1234567890");
        req.setBeneficiaryName("Test User");

        assertDoesNotThrow(() ->
                TransactionValidator.validateBeneficiary(req));
    }

    @Test
    void validateBeneficiary_nullRequest_shouldThrowException() {
        assertThrows(TransactionException.class,
                () -> TransactionValidator.validateBeneficiary(null));
    }

    @Test
    void validateBeneficiary_nullAccount_shouldThrowException() {
        BeneficiaryRequest req = new BeneficiaryRequest();
        req.setBeneficiaryAccount(null);
        req.setBeneficiaryName("Test User");

        assertThrows(TransactionException.class,
                () -> TransactionValidator.validateBeneficiary(req));
    }

    @Test
    void validateBeneficiary_nullName_shouldThrowException() {
        BeneficiaryRequest req = new BeneficiaryRequest();
        req.setBeneficiaryAccount("1234567890");
        req.setBeneficiaryName(null);

        assertThrows(TransactionException.class,
                () -> TransactionValidator.validateBeneficiary(req));
    }
}
