package com.bank.transaction_service.client;

record BankBranchInfo(
        String bankName,
        String branchName,
        String city,
        String ifscCode
) {
}
