package com.bank.transaction_service.service;

import com.bank.transaction_service.dto.request.CreditTransactionRequest;
import com.bank.transaction_service.dto.request.DebitTransactionRequest;
import com.bank.transaction_service.dto.request.TransferTransactionRequest;
import com.bank.transaction_service.dto.response.*;

public interface TransactionService {

    DebitTransactionResponse debit(DebitTransactionRequest request);

    CreditTransactionResponse credit(CreditTransactionRequest request);

    TransferTransactionResponse transfer(TransferTransactionRequest request);
}
