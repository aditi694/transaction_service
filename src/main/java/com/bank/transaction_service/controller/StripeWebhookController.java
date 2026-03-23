package com.bank.transaction_service.controller;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.impl.TransactionSagaService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final TransactionRepository transactionRepository;
    private final TransactionSagaService sagaService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public void handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws Exception {

        Event event;

        try {
            event = com.stripe.net.Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid Stripe signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {

            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (session == null) return;

            String paymentIntentId = session.getPaymentIntent();

            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            Map<String, String> metadata = intent.getMetadata();

            String txnId = metadata.get("transaction_id");

            Transaction tx = transactionRepository.findById(txnId).orElse(null);
            if (tx == null) return;

            TransactionSaga saga = sagaService.start(tx);
            sagaService.processCredit(tx, saga);
        }
    }
}