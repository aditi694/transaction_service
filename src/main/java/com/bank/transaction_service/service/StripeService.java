//package com.bank.transaction_service.service;
//
//import com.stripe.model.checkout.Session;
//import com.stripe.param.checkout.SessionCreateParams;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Service
//public class StripeService {
//
//    @Value("${stripe.success.url}")
//    private String successUrl;
//
//    @Value("${stripe.cancel.url}")
//    private String cancelUrl;
//
//    public Session createSession(String txnId, Long amount, String accountNumber) throws Exception {
//
//        SessionCreateParams params =
//                SessionCreateParams.builder()
//                        .setMode(SessionCreateParams.Mode.PAYMENT)
//                        .setSuccessUrl(successUrl)
//                        .setCancelUrl(cancelUrl)
//                        .setPaymentIntentData(
//                                SessionCreateParams.PaymentIntentData.builder()
//                                        .putMetadata("transaction_id", txnId)
//                                        .putMetadata("account_number", accountNumber)
//                                        .build()
//                        )
//                        .addLineItem(
//                                SessionCreateParams.LineItem.builder()
//                                        .setQuantity(1L)
//                                        .setPriceData(
//                                                SessionCreateParams.LineItem.PriceData.builder()
//                                                        .setCurrency("inr")
//                                                        .setUnitAmount(amount)
//                                                        .setProductData(
//                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
//                                                                        .setName("Wallet Top-up")
//                                                                        .build()
//                                                        )
//                                                        .build()
//                                        )
//                                        .build()
//                        )
//                        .build();
//
//        return Session.create(params);
//    }
//}