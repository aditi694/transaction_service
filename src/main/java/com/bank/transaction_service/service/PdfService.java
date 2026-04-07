package com.bank.transaction_service.service;

import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

public interface PdfService {
    void generateTransactionPdf(String accountNumber,
                                String fromDate,
                                String toDate,
                                HttpServletResponse response);

    void sendPdfToEmail(String accountNumber,
                        String fromDate,
                        String to,
                        UUID customerId);
}