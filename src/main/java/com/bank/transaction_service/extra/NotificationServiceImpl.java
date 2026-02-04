//package com.bank.transaction_service.service.impl;
//
//import com.bank.transaction_service.entity.Notification;
//import com.bank.transaction_service.entity.Transaction;
//import com.bank.transaction_service.enums.NotificationChannel;
//import com.bank.transaction_service.enums.NotificationStatus;
//import com.bank.transaction_service.exception.TransactionException;
//import com.bank.transaction_service.repository.NotificationRepository;
//import com.bank.transaction_service.repository.TransactionRepository;
//import com.bank.transaction_service.service.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationServiceImpl implements NotificationService {
//
//    private final NotificationRepository repository;
//    private final TransactionRepository transactionRepository;
//
//    @Override
//    public void sendTransactionAlert(String transactionId) {
//
//        Transaction tx = transactionRepository
//                .findByTransactionId(transactionId)
//                .orElseThrow(TransactionException::transactionNotFound);
//
//        Notification notification = Notification.builder()
//                .id(UUID.randomUUID())
//                .customerId(tx.getCustomerId())
//                .transactionId(tx.getTransactionId())
//                .channel(NotificationChannel.SMS)
//                .message(buildMessage(tx))
//                .sentAt(LocalDateTime.now())
//                .status(NotificationStatus.SENT)
//                .build();
//
//        repository.save(notification);
//
//        log.info("Transaction alert sent for txnId={}", tx.getTransactionId());
//    }
//
//    private String buildMessage(Transaction tx) {
//        return String.format(
//                "Your account %s has been %s ₹%s. Balance: ₹%s. Txn ID: %s",
//                maskAccount(tx.getAccountNumber()),
//                tx.getTransactionType().name().equals("CREDIT") ? "credited with" : "debited by",
//                tx.getTotalAmount(),
//                tx.getBalanceAfter(),
//                tx.getTransactionId()
//        );
//    }
//
//    private String maskAccount(String accountNumber) {
//        return "XXXX-XXXX-" + accountNumber.substring(accountNumber.length() - 4);
//    }
//}
