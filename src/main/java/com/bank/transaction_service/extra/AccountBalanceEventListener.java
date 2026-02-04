//// transaction-service/kafka/consumer/AccountBalanceEventListener.java
//package com.bank.transaction_service.kafka.consumer;
//
//import com.bank.transaction_service.entity.Transaction;
//import com.bank.transaction_service.enums.TransactionStatus;
//import com.bank.transaction_service.kafka.event.AccountBalanceEvent;
//import com.bank.transaction_service.repository.TransactionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class AccountBalanceEventListener {
//
//    private final TransactionRepository transactionRepo;
//
//    @KafkaListener(
//            topics = "account-balance-events",
//            groupId = "transaction-service"
//    )
//    public void handle(AccountBalanceEvent event) {
//
//        log.info("ACCOUNT BALANCE EVENT RECEIVED: {}", event);
//
//        Transaction tx = transactionRepo.findByTransactionId(event.transactionId())
//                .orElseThrow();
//
//        // ❌ Failure case
//        if ("FAILED".equals(event.status())) {
//            tx.setStatus(TransactionStatus.FAILED);
//            tx.setFailureReason(event.failureReason());
//            tx.setCompletedAt(LocalDateTime.now());
//            transactionRepo.save(tx);
//            return;
//        }
//
//        // ✅ Success case
//        if ("SUCCESS".equals(event.status())) {
//
//            tx.setCurrentBalance(event.balanceAfter());
//
//            // CREDIT or DEBIT → both finish here
//            tx.setStatus(TransactionStatus.SUCCESS);
//            tx.setCompletedAt(LocalDateTime.now());
//
//            transactionRepo.save(tx);
//        }
//    }
//}