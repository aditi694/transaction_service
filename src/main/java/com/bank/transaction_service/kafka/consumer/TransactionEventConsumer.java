//package com.bank.transaction_service.kafka.consumer;
//
//import com.bank.transaction_service.kafka.event.TransactionEvent;
//import com.bank.transaction_service.service.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TransactionEventConsumer {
//
//    private final NotificationService notificationService;
//
//    @KafkaListener(
//            topics = "transaction-events",
//            groupId = "transaction-service"
//    )
//    public void consume(TransactionEvent event) {
//
//        log.info("📥 Received transaction event: {}", event.getTransactionId());
//
//        if (event.getStatus().name().equals("SUCCESS")) {
//            notificationService.sendTransactionAlert(event.getTransactionId());
//        }
//
//        // 🔮 Future:
//        // analytics update
//        // fraud check
//        // reporting
//    }
//}
