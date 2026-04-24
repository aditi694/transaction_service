package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.response.NotificationResponse;
import com.bank.transaction_service.entity.Notification;
import com.bank.transaction_service.enums.NotificationStatus;
import com.bank.transaction_service.repository.NotificationRepository;
import com.bank.transaction_service.service.NotificationService;
import com.bank.transaction_service.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repo;
    private final NotificationWebSocketHandler websocketHandler;

    @Override
    public void createNotification(UUID userId,
                                   String transactionId,
                                   BigDecimal amount,
                                   String status,
                                   String accountNumber) {

        String message;

        if ("SUCCESS".equals(status)) {
            message = "₹" + amount + " transaction successful for account XXXX"
                    + accountNumber.substring(accountNumber.length() - 4);
        } else {
            message = "Transaction failed for ₹" + amount;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .transactionId(transactionId)
                .message(message)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(notification);

        websocketHandler.sendToUser(userId, NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .status(notification.getStatus().name())
                .createdAt(notification.getCreatedAt())
                .build());
    }

    @Override
    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .status(n.getStatus().name())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }
    @Override
    public void createNotification(UUID userId, String message, String type) {

        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(notification);

        websocketHandler.sendToUser(userId,
                NotificationResponse.builder()
                        .id(notification.getId())
                        .message(notification.getMessage())
                        .status(notification.getStatus().name())
                        .createdAt(notification.getCreatedAt())
                        .build()
        );
    }

    @Override
    public void markAsRead(UUID notificationId) {
        Notification n = repo.findById(notificationId).orElseThrow();
        n.setStatus(NotificationStatus.READ);
        repo.save(n);
    }
}