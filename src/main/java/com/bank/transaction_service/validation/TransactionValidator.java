package com.bank.transaction_service.validation;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.exception.TransactionException;

public final class TransactionValidator {

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
