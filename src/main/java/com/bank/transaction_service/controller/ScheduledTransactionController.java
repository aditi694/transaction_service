package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.ScheduleTransactionRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class ScheduledTransactionController {

    @PostMapping("/schedule")
    public String schedule(@RequestBody ScheduleTransactionRequest request) {
        return "Scheduled successfully";
    }
}
