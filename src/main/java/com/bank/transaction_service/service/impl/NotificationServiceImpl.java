package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.Notification;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.NotificationChannel;
import com.bank.transaction_service.enums.NotificationStatus;
import com.bank.transaction_service.repository.NotificationRepository;
import com.bank.transaction_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public void sendTransactionAlert(Transaction tx) {

        Notification n = Notification.builder()
                .id(UUID.randomUUID())
                .transactionId(tx.getTransactionId())
                .channel(NotificationChannel.valueOf("SMS"))
                .message("Transaction alert: " + tx.getTransactionType())
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.valueOf("SENT"))
                .build();


        repository.save(n);
    }
}
