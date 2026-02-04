//package com.bank.transaction_service.kafka.event;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//public record AccountBalanceEvent(
//        String transactionId,
//        String step,          // DEBIT | CREDIT | COMPENSATE
//        String status,        // SUCCESS | FAILED
//        String accountNumber,
//        BigDecimal balanceAfter,
//        String failureReason
//) {}