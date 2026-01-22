//package com.bank.transaction_service.util;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//public final class TransactionIdGenerator {
//
//    private TransactionIdGenerator() {}
//
//    public static String generate() {
//        return "TXN" + LocalDateTime.now()
//                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
//    }
//}
