//package com.bank.transaction_service.kafka.consumer;
//
//import com.bank.transaction_service.entity.Transaction;
//import com.bank.transaction_service.entity.TransactionSaga;
//import com.bank.transaction_service.enums.SagaStatus;
//import com.bank.transaction_service.enums.TransactionStatus;
//import com.bank.transaction_service.kafka.event.AccountBalanceEvent;
//import com.bank.transaction_service.repository.TransactionRepository;
//import com.bank.transaction_service.repository.TransactionSagaRepository;
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
//public class AccountBalanceEventConsumer {
//
//    private final TransactionRepository repo;
//
//    @KafkaListener(topics = "account-balance-events", groupId = "transaction-service")
//    public void handle(AccountBalanceEvent event) {
//
//        Transaction tx = repo.findByTransactionId(event.transactionId())
//                .orElseThrow();
//
//        if ("SUCCESS".equals(event.status())) {
//            tx.setStatus(TransactionStatus.SUCCESS);
//            tx.setCompletedAt(LocalDateTime.now());
//        } else {
//            tx.setStatus(TransactionStatus.FAILED);
//            tx.setFailureReason(event.failureReason());
//            tx.setCompletedAt(LocalDateTime.now());
//        }
//
//        repo.save(tx);
//        log.info("✅ Transaction {} completed with status {}",
//                tx.getTransactionId(), tx.getStatus());
//    }
//}