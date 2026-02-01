//package com.bank.transaction_service.config;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class KafkaConfig {
//
//    public static final String TRANSACTION_TOPIC = "transaction-events";
//
//    @Bean
//    public NewTopic transactionTopic() {
//        return new NewTopic(TRANSACTION_TOPIC, 3, (short) 1);
//    }
//}
