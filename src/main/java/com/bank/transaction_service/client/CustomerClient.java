package com.bank.transaction_service.client;

import com.bank.transaction_service.service.impl.BeneficiaryServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "CUSTOMER-SERVICE",
        path = "/api/internal/customers"
)
public interface CustomerClient {

    @GetMapping("/{customerId}/contact")
    String getContact(@PathVariable UUID customerId);

    @GetMapping("/account/{accountNumber}/ifsc")
    String getIfscByAccount(@PathVariable String accountNumber);

    @GetMapping("/bank-branch/{ifscCode}")
    BeneficiaryServiceImpl.BankBranchInfo getBankBranch(@PathVariable String ifscCode);

    @GetMapping("/email")
    String getEmail(@RequestParam UUID customerId);

    @GetMapping("/{customerId}/name")
    String getCustomerName(@PathVariable UUID customerId);

    @GetMapping("/account/{accountNumber}/name")
    String getCustomerNameByAccount(@PathVariable String accountNumber);

    @GetMapping("/account/{accountNumber}/email")
    String getEmailByAccount(@PathVariable String accountNumber);
}
