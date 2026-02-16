package com.bank.transaction_service.validation;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.exception.TransactionException;

//new change
public final class TransactionValidator {
    private TransactionValidator() {
    }

    public static void validateBeneficiary(
            BeneficiaryRequest req
    ) {
        if (req == null ||
                req.getBeneficiaryAccount() == null ||
                req.getBeneficiaryName() == null) {

            throw TransactionException.badRequest(
                    "Invalid beneficiary details"
            );
        }
    }
}
