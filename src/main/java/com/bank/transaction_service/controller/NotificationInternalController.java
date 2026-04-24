package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.InternalNotificationRequest;
import com.bank.transaction_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
public class NotificationInternalController {

    private final NotificationService notificationService;

    @PostMapping
    public void createNotification(@RequestBody InternalNotificationRequest req) {

        notificationService.createNotification(
                req.getUserId(),
                req.getMessage(),
                req.getType()
        );
    }
}