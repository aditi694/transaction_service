package com.bank.transaction_service.kafka.event;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionStatusListener {

    private final TransactionRepository repo;

    @KafkaListener(topics = "transaction-status", groupId = "transaction-service")
    public void handle(TransactionStatusEvent event) {

        Transaction tx = repo.findByTransactionId(event.transactionId())
                .orElseThrow();

        tx.setStatus(TransactionStatus.valueOf(event.status()));
        tx.setFailureReason(event.failureReason());
        tx.setCompletedAt(event.completedAt());

        repo.save(tx);
    }
}