package com.bank.transaction_service.validation;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.exception.TransactionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionValidatorTest {

    @Test
    void testValidateBeneficiary_Valid() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setBeneficiaryAccount("1234567890");
        request.setBeneficiaryName("John Doe");

        assertDoesNotThrow(() ->
                TransactionValidator.validateBeneficiary(request)
        );
    }

    @Test
    void testValidateBeneficiary_NullRequest() {
        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> TransactionValidator.validateBeneficiary(null)
        );

        assertEquals("Invalid beneficiary details", ex.getMessage());
        assertEquals("BAD_REQUEST", ex.getErrorCode());
    }

    @Test
    void testValidateBeneficiary_NullAccount() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setBeneficiaryName("John Doe");

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> TransactionValidator.validateBeneficiary(request)
        );

        assertEquals("Invalid beneficiary details", ex.getMessage());
    }

    @Test
    void testValidateBeneficiary_NullName() {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setBeneficiaryAccount("1234567890");

        TransactionException ex = assertThrows(
                TransactionException.class,
                () -> TransactionValidator.validateBeneficiary(request)
        );

        assertEquals("Invalid beneficiary details", ex.getMessage());
    }
}
