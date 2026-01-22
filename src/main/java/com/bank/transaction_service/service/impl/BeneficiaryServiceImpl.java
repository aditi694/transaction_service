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

        // 🔒 Check beneficiary account exists
        if (!accountClient.accountExists(req.getBeneficiaryAccount())) {
            throw TransactionException.badRequest("Beneficiary account does not exist");
        }

        // 🔥 FETCH PAYER IFSC FROM CUSTOMER SERVICE
        String payerIfsc;
        try {
            payerIfsc = customerClient.getIfscByAccount(req.getAccountNumber());
        } catch (FeignException e) {
            throw TransactionException.externalServiceError(
                    "Customer service unavailable"
            );
        }

        String payerBank = resolveBankNameFromIfsc(payerIfsc);
        String beneficiaryBank = resolveBankNameFromIfsc(req.getIfscCode());

        boolean autoVerified =
                payerBank.equalsIgnoreCase(beneficiaryBank);

        Beneficiary entity = Beneficiary.builder()
                .beneficiaryId("BEN-" + System.currentTimeMillis())
                .customerId(req.getCustomerId())
                .accountNumber(req.getAccountNumber())
                .beneficiaryName(req.getBeneficiaryName())
                .beneficiaryAccount(req.getBeneficiaryAccount())
                .ifscCode(req.getIfscCode())
                .bankName(beneficiaryBank)

                // ✅ CORRECT AUTO-VERIFY
                .isVerified(autoVerified)
                .verifiedAt(autoVerified ? LocalDateTime.now() : null)

                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        repository.save(entity);

        return BeneficiaryResponse.from(entity);
    }
    private boolean isSameBank(String bank1, String bank2) {
        return bank1 != null && bank1.equalsIgnoreCase(bank2);
    }
    @Override
    public BeneficiaryResponse get(String beneficiaryId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() ->
                        TransactionException.badRequest("Beneficiary not found"));

        return BeneficiaryResponse.from(b);
    }

    private String resolveBankNameFromIfsc(String ifsc) {
        if (ifsc == null || ifsc.length() < 4) return "UNKNOWN";

        return switch (ifsc.substring(0, 4)) {
            case "ICIC" -> "ICICI";
            case "HDFC" -> "HDFC";
            case "SBIN" -> "SBI";
            default -> "OTHER";
        };
    }

    // ---------------- CUSTOMER ----------------

    @Override
    public List<BeneficiaryResponse> list(String customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    // ---------------- ADMIN ----------------

    @Override
    public void adminVerify(String beneficiaryId, UUID adminId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() -> TransactionException.badRequest("Beneficiary not found"));

        b.setVerified(true);
        b.setVerifiedAt(LocalDateTime.now());
        b.setVerifiedBy(adminId.toString());
        b.setUpdatedAt(LocalDateTime.now());

        repository.save(b);
    }

    @Override
    public void reject(String beneficiaryId) {
        Beneficiary b = repository.findById(beneficiaryId)
                .orElseThrow(() -> TransactionException.badRequest("Beneficiary not found"));

        b.setActive(false);
        b.setUpdatedAt(LocalDateTime.now());
        repository.save(b);
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
}
