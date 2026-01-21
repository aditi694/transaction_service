package com.bank.transaction_service.validation;

import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.exception.TransactionException;

import java.math.BigDecimal;

public final class TransactionValidator {

    private TransactionValidator() {}

    /* ================= COMMON ================= */

    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw TransactionException.badRequest(
                    "Account number is required"
            );
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw TransactionException.badRequest(
                    "Transaction amount must be greater than zero"
            );
        }
    }

    /* ================= DEBIT / CREDIT ================= */

    public static void validateBalance(
            BigDecimal balance,
            BigDecimal amount
    ) {
        if (balance.compareTo(amount) < 0) {
            throw TransactionException.insufficientBalance();
        }
    }

    /* ================= TRANSFER ================= */

    public static void validateTransfer(
            String fromAccount,
            String toAccount
    ) {
        if (fromAccount.equals(toAccount)) {
            throw TransactionException.badRequest(
                    "Sender and receiver account cannot be same"
            );
        }
    }

    /* ================= BENEFICIARY ================= */

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

    /* ================= LIMITS ================= */

    public static void validateLimits(
            LimitUpdateRequest req
    ) {
        if (req.getPerTransactionLimit()
                .compareTo(req.getDailyLimit()) > 0) {

            throw TransactionException.badRequest(
                    "Per transaction limit cannot exceed daily limit"
            );
        }
    }
}
