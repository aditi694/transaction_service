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
    ) {

        System.out.println("WEBHOOK RECEIVED");

        try {
            Event event = com.stripe.net.Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );

            System.out.println(" Event Type: " + event.getType());

            if ("checkout.session.completed".equals(event.getType())) {

                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);

                if (session == null) {
                    System.out.println("❌ Session is null");
                    return;
                }

                String paymentIntentId = session.getPaymentIntent();
                System.out.println("💳 PaymentIntent: " + paymentIntentId);

                PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

                Map<String, String> metadata = intent.getMetadata();
                String txnId = metadata.get("transaction_id");

                System.out.println("🧾 TXN ID: " + txnId);

                Transaction tx = transactionRepository.findById(txnId).orElse(null);

                if (tx == null) {
                    System.out.println("❌ Transaction NOT FOUND");
                    return;
                }

                System.out.println("✅ Transaction FOUND");

                TransactionSaga saga = sagaService.start(tx);

                System.out.println("🚀 Starting CREDIT...");

                sagaService.processCredit(tx, saga);

                System.out.println("✅ CREDIT DONE");
            }

        } catch (Exception e) {
            System.out.println("❌ WEBHOOK ERROR:");
            e.printStackTrace();
        }
    }
}