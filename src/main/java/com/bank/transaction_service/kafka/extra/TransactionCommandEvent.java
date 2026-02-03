//package com.bank.transaction_service.kafka.event;
//
//import java.math.BigDecimal;
//
//public record TransactionCommandEvent(
//        String transactionId,
//        String step,          // DEBIT | CREDIT | COMPENSATE
//        String accountNumber,
//        BigDecimal amount
//) {}