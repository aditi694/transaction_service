package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import com.bank.transaction_service.service.ScheduledTransactionService;
import com.bank.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTransactionServiceImpl
        implements ScheduledTransactionService {

    private final ScheduledTransactionRepository repository;
    private final TransactionService transactionService;

    /**
     * Runs every day at 2 AM
     */
    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void executeScheduledTransactions() {

        List<ScheduledTransaction> dueTransactions =
                repository.findDueTransactions(LocalDate.now());


        for (ScheduledTransaction st : dueTransactions) {
            try {
                // reuse normal debit flow
                transactionService.debit(st.toDebitRequest(), null);

                st.updateNextExecutionDate();
            } catch (Exception e) {
                st.markFailed();
            }
        }
    }
}
