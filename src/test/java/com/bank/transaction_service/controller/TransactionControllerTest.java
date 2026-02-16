package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.CreditTransactionRequest;
import com.bank.transaction_service.dto.request.DebitTransactionRequest;
import com.bank.transaction_service.dto.request.TransferTransactionRequest;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    public void testDebit() {
        DebitTransactionRequest request = new DebitTransactionRequest();
        DebitTransactionResponse response = new DebitTransactionResponse();
        response.setMessage("hey");
        when(transactionService.debit(any())).thenReturn(response);
        ResponseEntity<BaseResponse<DebitTransactionResponse>> debit = transactionController.debit(request);
        Assertions.assertNotNull(debit);
        Assertions.assertEquals("Debit transaction initiated", debit.getBody().getResultInfo().getResultMsg());
    }

    @Test
    public void testCredit() {
        CreditTransactionRequest request = new CreditTransactionRequest();
        CreditTransactionResponse response = new CreditTransactionResponse();
        response.setMessage("COMPLETED");
        when(transactionService.credit(any())).thenReturn(response);
        ResponseEntity<BaseResponse<CreditTransactionResponse>> credit = transactionController.credit(request);
        Assertions.assertNotNull(credit);
        Assertions.assertEquals("Credit transaction initiated", credit.getBody().getResultInfo().getResultMsg());
    }

    @Test
    public void testTransfer() {
        TransferTransactionRequest request = new TransferTransactionRequest();
        TransferInitiatedResponse response = new TransferInitiatedResponse();
        response.setStatus("COMPLETED");
        when(transactionService.transfer(any())).thenReturn(response);
        ResponseEntity<BaseResponse<TransferInitiatedResponse>> transfer = transactionController.transfer(request);
        Assertions.assertNotNull(transfer);
        Assertions.assertEquals("Transfer transaction initiated", transfer.getBody().getResultInfo().getResultMsg());
    }

    @Test
    public void testGetStatus() {
        UUID transactionId = UUID.randomUUID();
        TransactionStatusResponse response = new TransactionStatusResponse();
        response.setStatus("COMPLETED");
        when(transactionService.getStatus(any())).thenReturn(response);
        ResponseEntity<BaseResponse<TransactionStatusResponse>> getStatus = transactionController.getStatus(String.valueOf(transactionId));
        Assertions.assertNotNull(getStatus);
        Assertions.assertEquals("Transaction status fetched successfully", getStatus.getBody().getResultInfo().getResultMsg());
    }

}