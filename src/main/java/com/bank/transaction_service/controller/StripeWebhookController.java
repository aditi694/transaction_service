//package com.bank.transaction_service.controller;
//
//import com.bank.transaction_service.entity.Transaction;
//import com.bank.transaction_service.entity.TransactionSaga;
//import com.bank.transaction_service.enums.TransactionStatus;
//import com.bank.transaction_service.repository.TransactionRepository;
//import com.bank.transaction_service.repository.TransactionSagaRepository;
//import com.bank.transaction_service.service.impl.TransactionSagaService;
//import com.stripe.model.Event;
//import com.stripe.model.checkout.Session;
//import com.stripe.model.PaymentIntent;
//import com.stripe.net.Webhook;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//public class StripeWebhookController {
//
//    private final TransactionRepository transactionRepository;
//    private final TransactionSagaRepository sagaRepository;
//    private final TransactionSagaService sagaService;
//
//    @Value("${stripe.webhook.secret}")
//    private String webhookSecret;
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(
//            @RequestBody String payload,
//            @RequestHeader("Stripe-Signature") String sigHeader
//    ) {
//
//        log.info("🔥 WEBHOOK RECEIVED");
//
//        try {
//            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
//
//            log.info("Event Type: {}", event.getType());
//
//            if ("checkout.session.completed".equals(event.getType())) {
//
//                Session session = (Session) event.getDataObjectDeserializer()
//                        .getObject()
//                        .orElse(null);
//
//                if (session == null) {
//                    log.error("Session is null");
//                    return ResponseEntity.ok("ignored");
//                }
//
//                String paymentIntentId = session.getPaymentIntent();
//
//                PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
//
//                String txnId = intent.getMetadata().get("transaction_id");
//
//                log.info("Transaction ID from Stripe: {}", txnId);
//
//                // ✅ FIX 1: CORRECT FETCH
//                Transaction tx = transactionRepository
//                        .findByTransactionId(txnId)
//                        .orElseThrow(() -> new RuntimeException("Transaction not found"));
//
//                log.info("Transaction FOUND");
//
//                // ✅ FIX 2: UPDATE PAYMENT STATUS FIRST
//                tx.setStatus(TransactionStatus.IN_PROGRESS);
//                transactionRepository.save(tx);
//
//                // ✅ FIX 3: DO NOT CREATE NEW SAGA
//                TransactionSaga saga = (TransactionSaga) sagaRepository
//                        .findByTransactionId(txnId)
//                        .orElseThrow(() -> new RuntimeException("Saga not found"));
//
//                log.info("Starting CREDIT process...");
//
//                sagaService.processCredit(tx, saga);
//
//                log.info("CREDIT COMPLETED");
//            }
//
//            return ResponseEntity.ok("success");
//
//        } catch (Exception e) {
//            log.error("WEBHOOK ERROR", e);
//            return ResponseEntity.status(500).body("error");
//        }
//    }
//}