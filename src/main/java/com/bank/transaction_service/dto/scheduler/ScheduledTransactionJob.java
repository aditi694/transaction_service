package com.bank.transaction_service.dto.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledTransactionJob {

    // Every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeScheduledTransactions() {

        log.info("Running scheduled transactions executor");

        // 1. Fetch ACTIVE scheduled transactions
        // 2. Check next_execution_date
        // 3. Call debit/credit internally
        // 4. Update next_execution_date
        // 5. Mark COMPLETED if endDate crossed
    }
}
