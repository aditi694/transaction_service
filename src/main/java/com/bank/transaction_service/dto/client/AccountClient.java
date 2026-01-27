package com.bank.transaction_service.dto.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(
        name = "ACCOUNT-SERVICE",
        path = "/api/internal/accounts"
)
public interface AccountClient {

    @PostMapping("/{accountNumber}/debit")
    void debit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    );

    @PostMapping("/{accountNumber}/credit")
    void credit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    );

    @GetMapping("/{accountNumber}/balance")
    BigDecimal getBalance(@PathVariable String accountNumber);


    @GetMapping("/{accountNumber}/exists")
    boolean accountExists(@PathVariable String accountNumber);

    @GetMapping("/{accountNumber}/owner")
    UUID getAccountOwner(@PathVariable String accountNumber); // ✅ UUID

}