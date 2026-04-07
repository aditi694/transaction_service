package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.MiniStatementResponse;
import com.bank.transaction_service.dto.response.TransactionHistoryResponse;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.service.PdfService;
import com.bank.transaction_service.service.TransactionQueryService;
import com.bank.transaction_service.util.AppConstants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class TransactionQueryController {

    private final TransactionQueryService queryService;
    private final PdfService pdfService;

    @GetMapping("/transactions")
    public ResponseEntity<BaseResponse<TransactionHistoryResponse>> history(
            @RequestParam("account_number") String accountNumber,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "1") int page
    ) {
        getAuthUser();
        TransactionHistoryResponse response =
                queryService.getHistory(accountNumber, limit, page);
        return ResponseEntity.ok(
                BaseResponse.success(
                        response,
                        "Transaction history fetched successfully"
                )
        );
    }

    @GetMapping("/mini-statement")
    public ResponseEntity<BaseResponse<MiniStatementResponse>> miniStatement(
            @RequestParam("account_number") String accountNumber
    ) {
        getAuthUser();
        MiniStatementResponse response =
                queryService.miniStatement(accountNumber);
        return ResponseEntity.ok(
                BaseResponse.success(
                        response,
                        "Mini statement generated successfully"
                )
        );
    }

    @GetMapping("/transactions/pdf")
    public void downloadPdf(
            @RequestParam("account_number") String accountNumber,
            @RequestParam String from,
            @RequestParam String to,
            HttpServletResponse response
    ) {
        getAuthUser();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=statement.pdf");
        pdfService.generateTransactionPdf(accountNumber, from, to, response);
    }

    @GetMapping("/transactions/pdf/email")
    public ResponseEntity<String> sendPdfToEmail(
            @RequestParam("account_number") String accountNumber,
            @RequestParam String from,
            @RequestParam String to
    ) {
        AuthUser user = getAuthUser();
        pdfService.sendPdfToEmail(accountNumber, from, to, user.getCustomerId());
        return ResponseEntity.ok("PDF sent successfully");
    }

    private AuthUser getAuthUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof AuthUser)) {

            throw new AccessDeniedException(AppConstants.UNAUTHORIZED);
        }
        return (AuthUser) authentication.getPrincipal();
    }
}
