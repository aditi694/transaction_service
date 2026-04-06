package com.bank.transaction_service.service;

import jakarta.servlet.http.HttpServletResponse;

public interface PdfService {
    void generateTransactionPdf(String accountNumber,
                                String fromDate,
                                String toDate,
                                HttpServletResponse response);
}