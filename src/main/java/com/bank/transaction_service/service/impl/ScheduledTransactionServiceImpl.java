package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.DebitTransactionRequest;
import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import com.bank.transaction_service.service.ScheduledTransactionService;
import com.bank.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTransactionServiceImpl implements ScheduledTransactionService {

    private final ScheduledTransactionRepository repository;
    private final TransactionService transactionService;

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void executeScheduledTransactions() {

        log.info("Starting scheduled transaction execution");

        LocalDate today = LocalDate.now();

        List<ScheduledTransaction> dueTransactions =
                repository.findDueTransactions(today);

        log.info("Found {} scheduled transactions due for execution", dueTransactions.size());

        int successCount = 0;
        int failureCount = 0;

        for (ScheduledTransaction st : dueTransactions) {
            try {
                log.info("Processing scheduled transaction: {}", st.getId());
                DebitTransactionRequest request = st.toDebitRequest();
                transactionService.debit(request);

                st.updateNextExecutionDate();

                if (st.getEndDate() != null &&
                        st.getNextExecutionDate().isAfter(st.getEndDate())) {
                    st.setStatus(ScheduledStatus.COMPLETED);
                    log.info("Scheduled transaction {} completed (reached end date)", st.getId());
                } else {
                    log.info("Next execution date for {}: {}",
                            st.getId(), st.getNextExecutionDate());
                }

                repository.save(st);
                successCount++;

            } catch (Exception e) {
                log.error("Failed to execute scheduled transaction: {}", st.getId(), e);

                st.markFailed();
                repository.save(st);
                failureCount++;
            }
        }

        log.info("Scheduled transaction execution completed");
        log.info("Success: {}, Failed: {}", successCount, failureCount);
    }
}