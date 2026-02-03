//package com.bank.transaction_service.kafka.consumer;
//
//import com.bank.transaction_service.entity.Transaction;
//import com.bank.transaction_service.enums.TransactionStatus;
//import com.bank.transaction_service.kafka.event.TransactionStatusEvent;
//import com.bank.transaction_service.repository.TransactionRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class TransactionStatusListener {
//
//    private final TransactionRepository transactionRepository;
//
//    @KafkaListener(
//            topics = "transaction-status",
//            groupId = "transaction-service"
//    )
//    public void handle(TransactionStatusEvent event) {
//
//        log.info("FINAL STATUS EVENT RECEIVED: {}", event);
//
//        Transaction tx = transactionRepository
//                .findByTransactionId(event.transactionId())
//                .orElseThrow();
//
//        tx.setStatus(TransactionStatus.valueOf(event.finalStatus()));
//        tx.setFailureReason(event.failureReason());
//        tx.setCompletedAt(event.completedAt());
//
//        transactionRepository.save(tx);
//
//        log.info("✅ TRANSACTION UPDATED: txn={}, status={}",
//                tx.getTransactionId(), tx.getStatus());
//    }
//}