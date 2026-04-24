package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.response.NotificationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void createNotification(UUID userId,
                            String transactionId,
                            BigDecimal amount,
                            String status,
                            String accountNumber);
    void createNotification(UUID userId, String message, String type);
    List<NotificationResponse> getUserNotifications(UUID userId);

    void markAsRead(UUID notificationId);
}