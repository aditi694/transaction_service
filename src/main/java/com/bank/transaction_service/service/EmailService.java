package com.bank.transaction_service.service;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
public interface EmailService {
    void sendTransactionEmail(String to, String subject, String body);
}