package com.bank.transaction_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MarkAsReadRequest {
    private UUID notificationId;
}