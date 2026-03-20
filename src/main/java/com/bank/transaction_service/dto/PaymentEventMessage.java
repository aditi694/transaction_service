package com.bank.transaction_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentEventMessage {

    private String eventId;
    private String paymentId;
    private String paymentIntentId;
    private String userId;
    private Long amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}