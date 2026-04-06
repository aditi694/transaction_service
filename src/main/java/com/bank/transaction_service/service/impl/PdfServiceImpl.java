package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.service.PdfService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final TransactionRepository transactionRepository;

    @Override
    public void generateTransactionPdf(String accountNumber,
                                       String fromDate,
                                       String toDate,
                                       HttpServletResponse response) {

        try {
            LocalDateTime start = LocalDateTime.parse(fromDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(toDate + "T23:59:59");

            List<Transaction> transactions =
                    transactionRepository.findTransactionsForMonth(
                            accountNumber,
                            start,
                            end
                    );

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, response.getOutputStream());

            document.open();
            Font bankFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
            Paragraph bankName = new Paragraph("Union Bank", bankFont);
            bankName.setAlignment(Element.ALIGN_CENTER);
            document.add(bankName);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph title = new Paragraph("Transaction Statement\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            document.add(new Paragraph("Account Number: ****" +
                    accountNumber.substring(accountNumber.length() - 4), normal));
            document.add(new Paragraph("Period: " + fromDate + " to " + toDate, normal));
            document.add(new Paragraph("Generated on: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")), normal));

            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

            String[] headers = {"Date", "Time", "Type", "Description", "Amount", "Status"};

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy");
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a");

            for (Transaction tx : transactions) {

                table.addCell(tx.getCreatedAt().format(dateFormat));
                table.addCell(tx.getCreatedAt().format(timeFormat));
                table.addCell(tx.getTransactionType().name());
                table.addCell(tx.getDescription() != null ? tx.getDescription() : "-");

                Font amountFont = tx.getTransactionType().name().equals("DEBIT")
                        ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.RED)
                        : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GREEN);

                String sign = tx.getTransactionType().name().equals("DEBIT") ? "- ₹" : "+ ₹";
                table.addCell(new Phrase(sign + tx.getTotalAmount(), amountFont));

                table.addCell(tx.getStatus().name());
            }

            document.add(table);

            document.add(new Paragraph("\n\n--- End of Statement ---", normal));

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}