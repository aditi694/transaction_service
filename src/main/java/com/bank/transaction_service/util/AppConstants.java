package com.bank.transaction_service.util;

public final class AppConstants {

    private AppConstants() {}

    // ============ TRANSACTION STATUS MESSAGES ============
    public static final String TXN_SUCCESS_MSG =
            "Transaction completed successfully";

    public static final String TXN_PENDING_MSG =
            "Transaction is being processed";

    public static final String TXN_FAILED_MSG =
            "Transaction failed. Amount will be refunded if debited";

    public static final String TXN_IN_PROGRESS_MSG =
            "Transaction in progress. Please wait";

    // ============ TRANSACTION TYPE MESSAGES ============
    public static final String DEBIT_TXN_MSG =
            "Amount debited successfully";

    public static final String CREDIT_TXN_MSG =
            "Amount credited successfully";

    public static final String TRANSFER_TXN_MSG =
            "Transfer completed successfully";

    // ============ TRANSFER MODE MESSAGES ============
    public static final String NEFT_MSG =
            "NEFT transfer - Settles in 2 hours during banking hours";

    public static final String RTGS_MSG =
            "RTGS transfer - Real-time settlement (₹2L minimum)";

    public static final String IMPS_MSG =
            "IMPS transfer - Instant 24x7 transfer";

    public static final String UPI_MSG =
            "UPI transfer - Instant payment";

    // ============ LIMIT MESSAGES ============
    public static final String DAILY_LIMIT_MSG =
            "Daily transaction limit";

    public static final String PER_TXN_LIMIT_MSG =
            "Per transaction limit";

    public static final String MONTHLY_LIMIT_MSG =
            "Monthly transaction limit";

    public static final String ATM_LIMIT_MSG =
            "ATM withdrawal limit";

    // ============ BENEFICIARY MESSAGES ============
    public static final String BENEFICIARY_ADDED_MSG =
            "Beneficiary added successfully";

    public static final String BENEFICIARY_VERIFIED_MSG =
            "Beneficiary verified and ready for transfer";

    public static final String BENEFICIARY_PENDING_MSG =
            "Beneficiary pending admin verification";

    public static final String BENEFICIARY_REJECTED_MSG =
            "Beneficiary verification rejected";

    // ============ SCHEDULED TRANSACTION MESSAGES ============
    public static final String SCHEDULE_ACTIVE_MSG =
            "Scheduled transaction is active";

    public static final String SCHEDULE_PAUSED_MSG =
            "Scheduled transaction paused";

    public static final String SCHEDULE_COMPLETED_MSG =
            "Scheduled transaction completed";

    public static final String SCHEDULE_FAILED_MSG =
            "Scheduled transaction failed";

    public static final String SCHEDULE_CANCELLED_MSG =
            "Scheduled transaction cancelled";

    // ============ NOTIFICATION MESSAGES ============
    public static final String NOTIF_TXN_ALERT =
            "Transaction alert sent successfully";

    public static final String NOTIF_SENT_MSG =
            "Notification sent via SMS and Email";

    public static final String NOTIF_FAILED_MSG =
            "Failed to send notification";

    // ============ VALIDATION MESSAGES ============
    public static final String INVALID_AMOUNT_MSG =
            "Transaction amount must be greater than zero";

    public static final String INVALID_ACCOUNT_MSG =
            "Invalid account number format";

    public static final String SAME_ACCOUNT_TRANSFER_MSG =
            "Cannot transfer to the same account";

    public static final String INVALID_IFSC_MSG =
            "Invalid IFSC code format";

    // ============ ERROR MESSAGES ============
    public static final String INSUFFICIENT_BALANCE_MSG =
            "Insufficient balance in your account";

    public static final String LIMIT_EXCEEDED_MSG =
            "Transaction limit exceeded";

    public static final String DAILY_LIMIT_EXCEEDED_MSG =
            "Daily transaction limit exceeded";

    public static final String TXN_NOT_FOUND_MSG =
            "Transaction not found";

    public static final String BENEFICIARY_NOT_VERIFIED_MSG =
            "Beneficiary is not yet verified";

    // ============ SUCCESS TITLES ============
    public static final String TITLE_SUCCESS = "Success ✓";
    public static final String TITLE_PENDING = "Processing...";
    public static final String TITLE_FAILED = "Failed ✗";

    // ============ COMMON KEYS ============
    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    public static final String DESCRIPTION = "description";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String AMOUNT = "amount";
    public static final String BALANCE = "balance";
    public static final String STATUS = "status";
    public static final String DATA = "data";

}