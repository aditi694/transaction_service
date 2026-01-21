package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.Notification;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.NotificationChannel;
import com.bank.transaction_service.enums.NotificationStatus;
import com.bank.transaction_service.repository.NotificationRepository;
import com.bank.transaction_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public void sendTransactionAlert(Transaction tx) {

        String message = buildMessage(tx);

        Notification n = Notification.builder()
                .id(UUID.randomUUID())
                .customerId(tx.getCustomerId()) // ✅ Now using actual customerId from transaction
                .transactionId(tx.getTransactionId())
                .channel(NotificationChannel.SMS)
                .message(message)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.SENT)
                .build();

        repository.save(n);

        log.info("Notification sent for transaction: {} to customer: {}",
                tx.getTransactionId(), tx.getCustomerId());
    }

    private String buildMessage(Transaction tx) {
        String type = tx.getTransactionType().name();
        String amount = tx.getTotalAmount().toString();
        String balance = tx.getBalanceAfter().toString();

        return String.format(
                "Dear Customer, Your account %s has been %s by ₹%s. Available balance: ₹%s. " +
                        "Transaction ID: %s. If not done by you, call 1800-XXX-XXXX immediately.",
                maskAccount(tx.getAccountNumber()),
                type.equalsIgnoreCase("CREDIT") ? "credited" : "debited",
                amount,
                balance,
                tx.getTransactionId()
        );
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "XXXX";
        }
        return "XXXX-XXXX-" + accountNumber.substring(accountNumber.length() - 4);
    }
}