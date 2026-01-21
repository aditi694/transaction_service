package com.bank.transaction_service.entity;

import com.bank.transaction_service.enums.NotificationChannel;
import com.bank.transaction_service.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID customerId;
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime sentAt;
}
