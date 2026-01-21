package com.bank.transaction_service.service;

public interface ReceiptService {

    byte[] generateReceipt(String transactionId);
}
