package com.bank.transaction_service.dto.client;

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

}
