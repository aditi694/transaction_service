package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.TransactionAnalyticsResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class AnalyticsController {

    @GetMapping("/analytics")
    public TransactionAnalyticsResponse analytics(
            @RequestParam String month
    ) {
        return null; // service later
    }
}
