package com.bank.transaction_service.consumer;

import com.bank.transaction_service.dto.PaymentEventMessage;
import com.bank.transaction_service.service.PaymentIntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class PaymentEventConsumer {

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentIntegrationService integrationService;

    @Value("${payment.pubsub.subscription}")
    private String subscription;

    public PaymentEventConsumer(PubSubTemplate pubSubTemplate,
                                ObjectMapper objectMapper,
                                PaymentIntegrationService integrationService) {
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.integrationService = integrationService;
    }

    @PostConstruct
    public void subscribe() {
        pubSubTemplate.subscribe(subscription, message -> {
            try {
                String payload = message.getPubsubMessage().getData().toStringUtf8();

                PaymentEventMessage event =
                        objectMapper.readValue(payload, PaymentEventMessage.class);

                integrationService.handlePaymentSuccess(event);

                message.ack();

            } catch (Exception e) {
                message.nack();
            }
        });
    }
}