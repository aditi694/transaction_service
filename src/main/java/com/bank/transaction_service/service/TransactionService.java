package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.request.CreditTransactionRequest;
import com.bank.transaction_service.dto.request.DebitTransactionRequest;
import com.bank.transaction_service.dto.request.TransferTransactionRequest;
import com.bank.transaction_service.dto.response.*;

public interface TransactionService {

    DebitTransactionResponse debit(DebitTransactionRequest request, String idempotencyKey);

    CreditTransactionResponse credit(CreditTransactionRequest request, String idempotencyKey);

    TransferTransactionResponse transfer(TransferTransactionRequest request, String idempotencyKey);
}