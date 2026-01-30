//package com.bank.transaction_service.kafka.event;
//
//import com.bank.transaction_service.enums.TransactionStatus;
//import com.bank.transaction_service.enums.TransactionType;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class TransactionEvent {
//
//    private String transactionId;
//    private UUID customerId;
//
//    private TransactionType transactionType;
//    private TransactionStatus status;
//
//    private String fromAccount;
//    private String toAccount;
//
//    private BigDecimal amount;
//    private BigDecimal charges;
//    private BigDecimal totalAmount;
//
//    private String utrNumber;
//    private LocalDateTime occurredAt;
//}
