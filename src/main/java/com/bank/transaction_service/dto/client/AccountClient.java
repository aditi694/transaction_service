package com.bank.transaction_service.dto.client;

import com.bank.transaction_service.dto.request.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(
        name = "ACCOUNT-SERVICE",
        path = "/api/internal/accounts"
)
public interface AccountClient {

        // ✅ CREDIT
        @PostMapping("/{accountNumber}/credit")
        void credit(
                @PathVariable String accountNumber,
                @RequestParam BigDecimal amount,
                @RequestParam String transactionId
        );

        // ✅ DEBIT
        @PostMapping("/{accountNumber}/debit")
        void debit(
                @PathVariable String accountNumber,
                @RequestParam BigDecimal amount,
                @RequestParam String transactionId
        );

        @PostMapping("/transfer")
        void transfer(
                @RequestParam String fromAccount,
                @RequestParam String toAccount,
                @RequestParam BigDecimal amount,
                @RequestParam BigDecimal charges,
                @RequestParam String transactionId
        );

        // ✅ READ-ONLY calls
        @GetMapping("/{accountNumber}/balance")
        BigDecimal getBalance(@PathVariable String accountNumber);

        @GetMapping("/{accountNumber}/owner")
        UUID getAccountOwner(@PathVariable String accountNumber);

        @GetMapping("/{accountNumber}/exists")
        boolean accountExists(@PathVariable String accountNumber);

        @PostMapping("/update-balance")
        void updateBalance(@RequestBody BalanceUpdateRequest request);
}