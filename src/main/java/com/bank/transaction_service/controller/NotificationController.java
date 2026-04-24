package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.MarkAsReadRequest;
import com.bank.transaction_service.dto.response.NotificationResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public List<NotificationResponse> getNotifications() {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        return service.getUserNotifications(user.getCustomerId());
    }

    @PatchMapping("/read")
    public void markAsRead(@RequestBody MarkAsReadRequest req) {
        service.markAsRead(req.getNotificationId());
    }
}