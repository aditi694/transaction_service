//package com.bank.transaction_service.kafka.producer;
//
//import com.bank.transaction_service.config.KafkaConfig;
//import com.bank.transaction_service.kafka.event.TransactionEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class TransactionEventProducer {
//
//    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
//
//    public void publish(TransactionEvent event) {
//        kafkaTemplate.send(
//                KafkaConfig.TRANSACTION_TOPIC,
//                event.getTransactionId(),
//                event
//        );
//        log.info("📤 Published transaction event: {}", event.getTransactionId());
//    }
//}
