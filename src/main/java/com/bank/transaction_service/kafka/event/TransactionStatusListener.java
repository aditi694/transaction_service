package com.bank.transaction_service.kafka.event;

import com.bank.transaction_service.client.AccountClient;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionStatus;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionStatusListener {

    private final TransactionRepository repo;
    private final AccountClient accountClient;
    private final EmailService emailService;

    @KafkaListener(topics = "transaction-status", groupId = "transaction-service")
    public void handle(TransactionStatusEvent event) {

        Transaction tx = repo.findByTransactionId(event.transactionId())
                .orElseThrow();

        tx.setStatus(TransactionStatus.valueOf(event.finalStatus()));
        tx.setFailureReason(event.failureReason());
        tx.setCompletedAt(event.completedAt());
        if (tx.getStatus() == TransactionStatus.SUCCESS) {
            BigDecimal currentBalance =
                    accountClient.getBalance(tx.getAccountNumber());
            tx.setCurrentBalance(currentBalance);
        }

        repo.save(tx);

        log.info("Transaction {} moved to {}",
                tx.getTransactionId(), tx.getStatus());
        if (event.finalStatus().equals("SUCCESS") || event.finalStatus().equals("FAILED")) {

            String subject = "Transaction " + event.finalStatus();

            String body = """
                    Dear Customer,
                    
                    Your transaction has been successfully processed.
                    
                    ---------------------------------------
                    Transaction ID : %s
                    Amount         : ₹%s
                    Status         : %s
                    Date           : %s
                    ---------------------------------------
                    
                    Thank you for banking with us.
                    
                    UNION BANK Team
                    """.formatted(
                    event.transactionId(),
                    event.amount(),
                    event.finalStatus(),
                    event.completedAt()
            );

            try {
                emailService.sendTransactionEmail(event.email(), subject, body);
            } catch (Exception e) {
                log.error("Failed to send email for txn {}", event.transactionId(), e);
            }
        }
    }
}