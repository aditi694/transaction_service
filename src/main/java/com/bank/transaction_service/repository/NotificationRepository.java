package com.bank.transaction_service.repository;

import com.bank.transaction_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    List<Notification> findByCustomerId(UUID customerId);

    List<Notification> findByTransactionId(String transactionId);
}
