package com.bank.transaction_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class InternalNotificationRequest {

    private UUID userId;
    private String message;
    private String type;
}