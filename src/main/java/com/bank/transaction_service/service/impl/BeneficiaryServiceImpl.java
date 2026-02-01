package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.client.CustomerClient;
import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.entity.Beneficiary;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.BeneficiaryRepository;
import com.bank.transaction_service.service.BeneficiaryService;
import com.bank.transaction_service.validation.TransactionValidator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.bank.transaction_service.util.AppConstants.BENEFICIARY_PENDING_MSG;
import static com.bank.transaction_service.util.AppConstants.BENEFICIARY_VERIFIED_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository repository;
    private final AccountClient accountClient;
    private final CustomerClient customerClient;

    @Override
    public BeneficiaryResponse add(BeneficiaryRequest req) {

        TransactionValidator.validateBeneficiary(req);

        if (repository.existsByCustomerIdAndBeneficiaryAccount(
                req.getCustomerId(), req.getBeneficiaryAccount())) {
            throw TransactionException.badRequest(
                    "Beneficiary account already exists for this customer"
            );
        }

        if (!accountClient.accountExists(req.getBeneficiaryAccount())) {
            throw TransactionException.badRequest(
                    "Beneficiary account does not exist"
            );
        }
        String payerIfsc;
        try {
            payerIfsc = customerClient.getIfscByAccount(req.getAccountNumber());
        } catch (FeignException e) {
            log.error("Failed to fetch payer IFSC: {}", e.getMessage());
            throw TransactionException.externalServiceError(
                    "Customer service unavailable"
            );
        }

        String beneficiaryIfsc = req.getIfscCode();

        BankBranchInfo payerBank = fetchBankBranch(payerIfsc);
        BankBranchInfo beneficiaryBank = fetchBankBranch(beneficiaryIfsc);

        boolean autoVerified = payerBank.bankName.equalsIgnoreCase(
                beneficiaryBank.bankName
        );

        Beneficiary entity = Beneficiary.builder()
                .beneficiaryId("BEN-" + System.currentTimeMillis())
                .customerId(req.getCustomerId())
                .accountNumber(req.getAccountNumber())
                .beneficiaryName(req.getBeneficiaryName())
                .beneficiaryAccount(req.getBeneficiaryAccount())
                .ifscCode(beneficiaryIfsc)
                .bankName(beneficiaryBank.bankName)
                .branchName(beneficiaryBank.branchName)
                .isVerified(autoVerified)
                .verifiedAt(autoVerified ? LocalDateTime.now() : null)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        repository.save(entity);

        BeneficiaryResponse response = BeneficiaryResponse.from(entity);
        response.setMessage(autoVerified ? BENEFICIARY_VERIFIED_MSG : BENEFICIARY_PENDING_MSG);
        response.setStatusMessage(autoVerified ? "VERIFIED" : "PENDING_VERIFICATION");

        return BeneficiaryResponse.from(entity);
    }

    @Override
    public BeneficiaryResponse get(String beneficiaryId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() ->
                        TransactionException.badRequest("Beneficiary not found"));
        return BeneficiaryResponse.from(b);
    }

    @Override
    public List<BeneficiaryResponse> list(String customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    @Override
    public void adminVerify(String beneficiaryId, UUID adminId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() ->
                        TransactionException.badRequest("Beneficiary not found"));

        b.setVerified(true);
        b.setVerifiedAt(LocalDateTime.now());
        b.setVerifiedBy(adminId.toString());
        b.setUpdatedAt(LocalDateTime.now());

        repository.save(b);
        log.info("Beneficiary verified by admin: {}", beneficiaryId);
    }

    @Override
    public void reject(String beneficiaryId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() ->
                        TransactionException.badRequest("Beneficiary not found"));

        b.setActive(false);
        b.setUpdatedAt(LocalDateTime.now());
        repository.save(b);
        log.info("Beneficiary rejected: {}", beneficiaryId);
    }

    @Override
    public List<BeneficiaryResponse> listPendingApprovals() {
        return repository.findByIsVerifiedAndIsActive(false, true)
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    @Override
    public List<BeneficiaryResponse> listAll() {
        return repository.findAll()
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    private BankBranchInfo fetchBankBranch(String ifscCode) {
        try {
            return customerClient.getBankBranch(ifscCode);
        } catch (FeignException e) {
            log.warn("Failed to fetch bank branch for IFSC: {}", ifscCode);
            return new BankBranchInfo(
                    extractBankNameFromIfsc(ifscCode),
                    "Unknown Branch"
            );
        }
    }

    private String extractBankNameFromIfsc(String ifsc) {
        if (ifsc == null || ifsc.length() < 4) return "UNKNOWN";

        return switch (ifsc.substring(0, 4)) {
            case "ICIC" -> "ICICI Bank";
            case "HDFC" -> "HDFC Bank";
            case "SBIN" -> "State Bank of India";
            case "AXIS" -> "Axis Bank";
            case "PUNB" -> "Punjab National Bank";
            case "UBIN" -> "Union Bank of India";
            default -> "OTHER";
        };
    }

    public record BankBranchInfo(String bankName, String branchName) {}
}