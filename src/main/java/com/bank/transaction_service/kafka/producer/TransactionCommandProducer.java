package com.bank.transaction_service.kafka.producer;

import com.bank.transaction_service.kafka.event.TransactionCommandEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionCommandProducer {

    private final KafkaTemplate<String, TransactionCommandEvent> kafkaTemplate;

    public void send(TransactionCommandEvent event) {
        kafkaTemplate.send("transaction-commands", event.transactionId(), event);
    }
}