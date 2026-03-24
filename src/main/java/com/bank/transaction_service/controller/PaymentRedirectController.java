package com.bank.transaction_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentRedirectController {

    @GetMapping("/success")
    public String success() {
        return "✅ Payment successful. You can close this page.";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "❌ Payment cancelled.";
    }

    @GetMapping("/")
    public String home() {
        return "OK";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}