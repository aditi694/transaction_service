package com.bank.transaction_service.dto.client;

record BankBranchInfo(
        String bankName,
        String branchName,
        String city,
        String ifscCode
) {
}
