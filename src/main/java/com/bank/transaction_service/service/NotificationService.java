package com.bank.transaction_service.service;

import com.bank.transaction_service.entity.Transaction;

public interface NotificationService {

    void sendTransactionAlert(String transactionId);
}
