package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.client.AccountClient;
import com.bank.transaction_service.client.CustomerClient;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.EmailService;
import com.bank.transaction_service.service.PdfService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final TransactionRepository transactionRepository;
    private final CustomerClient customerClient;
    private final EmailService emailService;
    private final AccountClient accountClient;
    private final NotificationServiceImpl notificationService;

    @Override
    public void generateTransactionPdf(String accountNumber,
                                       String fromDate,
                                       String toDate,
                                       HttpServletResponse response) {
        try {
            ByteArrayOutputStream out = createStyledEncryptedPdf(accountNumber, fromDate, toDate);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=statement.pdf");
            response.getOutputStream().write(out.toByteArray());
            response.getOutputStream().flush();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
    // ================= EMAIL =================
    @Override
    public void sendPdfToEmail(String accountNumber,
                               String fromDate,
                               String toDate,
                               UUID customerId) {

        try {
            ByteArrayOutputStream out = createStyledEncryptedPdf(accountNumber, fromDate, toDate);

            String email = customerClient.getEmail(customerId);
            String fullName = customerClient.getCustomerName(customerId);
            String password = generatePassword(fullName, accountNumber);

            String body =
                    "<html>" +
                            "<body style='font-family: Arial;'>" +

                            "<div style='background:#004aad;color:white;padding:12px;text-align:center;'>" +
                            "<h2>Union Bank of India</h2>" +
                            "</div>" +

                            "<div style='padding:15px;'>" +
                            "<p>Dear " + fullName + ",</p>" +

                            "<p>Your account statement is attached.</p>" +

                            "<p><b>Password:</b> " + "Use ur name and account number" + "</p>" +

                            "<p style='color:gray;font-size:12px;'>" +
                            "This is a system-generated email. Do not reply." +
                            "</p>" +

                            "</div>" +
                            "</body>" +
                            "</html>";

            emailService.sendEmailWithAttachment(
                    email,
                    "Your Bank Statement",
                    body,
                    out.toByteArray()
            );
            notificationService.createNotification(
                    customerId,
                    "Your account statement has been sent to your email",
                    "STATEMENT"
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to send PDF email", e);
        }
    }

    private ByteArrayOutputStream createStyledEncryptedPdf(String accountNumber,
                                                           String fromDate,
                                                           String toDate) throws Exception {

        LocalDateTime start = LocalDateTime.parse(fromDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(toDate + "T23:59:59");

        List<Transaction> transactions =
                transactionRepository.findTransactionsForMonth(accountNumber, start, end);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 25, 25, 40, 30);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        UUID owner = accountClient.getAccountOwner(accountNumber);
        String customerName = customerClient.getCustomerName(owner);
        String email = customerClient.getEmail(owner);
        String ifsc = customerClient.getIfscByAccount(accountNumber);

        String branchName = "-";
        try {
            BeneficiaryServiceImpl.BankBranchInfo branchInfo = customerClient.getBankBranch(ifsc);
            if (branchInfo != null && branchInfo.branchName() != null) {
                branchName = branchInfo.branchName().trim();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch branch info for IFSC: {}", ifsc, e);
        }
        BigDecimal balance = accountClient.getBalance(accountNumber);
        String availableBalanceStr = (balance != null) ? balance.toString() : "-";

        String password = generatePassword(customerName, accountNumber);

        writer.setEncryption(
                password.getBytes(),
                "owner123".getBytes(),
                PdfWriter.ALLOW_PRINTING,
                PdfWriter.ENCRYPTION_AES_128
        );

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // ================= HEADER =================
        Paragraph bankName = new Paragraph("UNION BANK OF INDIA", titleFont);
        bankName.setAlignment(Element.ALIGN_CENTER);
        document.add(bankName);

        Paragraph statementTitle = new Paragraph("STATEMENT OF ACCOUNT", titleFont);
        statementTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(statementTitle);

        Paragraph stmtDate = new Paragraph("Date : " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yy")), normalFont);
        stmtDate.setAlignment(Element.ALIGN_RIGHT);
        document.add(stmtDate);

        document.add(new Paragraph("\n"));

        // ================= INFORMATION =================
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{3.2f, 6.8f});

        addInfoRow(infoTable, "Customer Name", customerName, boldFont, normalFont);
        addInfoRow(infoTable, "Branch", branchName, boldFont, normalFont);
        addInfoRow(infoTable, "Account Number", accountNumber, boldFont, normalFont);
        addInfoRow(infoTable, "Email", email, boldFont, normalFont);
        addInfoRow(infoTable, "IFSC", ifsc, boldFont, normalFont);
        addInfoRow(infoTable, "Available Balance",
                availableBalanceStr != null ? availableBalanceStr : "-", boldFont, normalFont);
        addInfoRow(infoTable, "Account Description", "REGULAR SAVINGS BANK ACCOUNT", boldFont, normalFont);
        addInfoRow(infoTable, "Currency", "INR", boldFont, normalFont);
        addInfoRow(infoTable, "Statement Period", fromDate + " to " + toDate, boldFont, normalFont);

        document.add(infoTable);
        document.add(new Paragraph("\n"));

        // ================= TRANSACTION TABLE =================
        PdfPTable txTable = new PdfPTable(7);
        txTable.setWidthPercentage(100);
        txTable.setWidths(new float[]{1.7f, 1.7f, 4.0f, 2.0f, 1.6f, 1.6f, 2.2f});

        BaseColor headerBg = new BaseColor(173, 216, 230);

        String[] headers = {"Txn Date", "Value Date", "Description", "Ref/Cheque No.", "Debit", "Credit", "Balance"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            txTable.addCell(cell);
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yy");
        for (Transaction tx : transactions) {
            BigDecimal amount = tx.getTotalAmount();
            String debit = "-";
            String credit = "-";
            if ("DEBIT".equals(tx.getTransactionType().name())) {
                debit = amount.toString();
            } else {
                credit = amount.toString();
            }
            String balanceStr = tx.getCurrentBalance() != null
                    ? tx.getCurrentBalance().toString()
                    : "-";
            addTransactionRow(txTable,
                    tx.getCreatedAt().format(df),
                    tx.getCreatedAt().format(df),
                    tx.getDescription() != null ? tx.getDescription() : "",
                    tx.getTransactionId() != null ? tx.getTransactionId() : "0000000",
                    debit,
                    credit,
                    balanceStr,
                    normalFont);
        }

        document.add(txTable);
        document.add(new Paragraph("\n"));

        Paragraph endStatement = new Paragraph("--- END OF STATEMENT ---", normalFont);
        endStatement.setAlignment(Element.ALIGN_CENTER);
        document.add(endStatement);

        Paragraph disclaimer = new Paragraph(
                "This is a system generated statement and does not require any signature.", smallFont);
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        document.add(disclaimer);

        document.close();
        return out;
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + " :", labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addTransactionRow(PdfPTable table, String txnDate, String valueDate, String description,
                                   String refNo, String debit, String credit, String balance, Font font) {

        table.addCell(new PdfPCell(new Phrase(txnDate, font)));
        table.addCell(new PdfPCell(new Phrase(valueDate, font)));
        table.addCell(new PdfPCell(new Phrase(description, font)));
        table.addCell(new PdfPCell(new Phrase(refNo, font)));

        PdfPCell debitCell = new PdfPCell(new Phrase(debit, font));
        debitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(debitCell);

        PdfPCell creditCell = new PdfPCell(new Phrase(credit, font));
        creditCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(creditCell);

        PdfPCell balanceCell = new PdfPCell(new Phrase(balance, font));
        balanceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(balanceCell);
    }

    // ================= PASSWORD LOGIC =================
    private String generatePassword(String fullName, String accountNumber) {
        String cleanName = fullName.replaceAll("\\s+", "");
        String first4 = cleanName.length() >= 4
                ? cleanName.substring(0, 4)
                : cleanName;

        String last4 = accountNumber.substring(accountNumber.length() - 4);
        return first4 + last4;
    }
}