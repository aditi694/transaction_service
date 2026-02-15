package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.dto.client.CustomerClient;
import com.bank.transaction_service.dto.request.BeneficiaryRequest;
import com.bank.transaction_service.dto.response.BeneficiaryResponse;
import com.bank.transaction_service.entity.Beneficiary;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.BeneficiaryRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceImplTest {

    @Mock private BeneficiaryRepository repository;
    @Mock private AccountClient accountClient;
    @Mock private CustomerClient customerClient;

    @InjectMocks
    private BeneficiaryServiceImpl service;

    private BeneficiaryRequest req;

    @BeforeEach
    void setup() {
        req = new BeneficiaryRequest();
        req.setCustomerId("C1");
        req.setAccountNumber("ACC1");
        req.setBeneficiaryName("John");
        req.setBeneficiaryAccount("BEN1");
        req.setIfscCode("HDFC0001");
    }

    private void mockBaseSuccess() {
        when(repository.existsByCustomerIdAndBeneficiaryAccount("C1","BEN1"))
                .thenReturn(false);
        when(accountClient.accountExists("BEN1"))
                .thenReturn(true);
    }

    @Test
    void add_whenDuplicate() {
        when(repository.existsByCustomerIdAndBeneficiaryAccount("C1","BEN1"))
                .thenReturn(true);
        assertThrows(TransactionException.class, () -> service.add(req));
    }

    @Test
    void add_whenAccountMissing() {
        when(repository.existsByCustomerIdAndBeneficiaryAccount("C1","BEN1"))
                .thenReturn(false);
        when(accountClient.accountExists("BEN1"))
                .thenReturn(false);
        assertThrows(TransactionException.class, () -> service.add(req));
    }

    @Test
    void add_whenCustomerFeignFails() {
        mockBaseSuccess();
        when(customerClient.getIfscByAccount("ACC1"))
                .thenThrow(mock(FeignException.class));
        assertThrows(TransactionException.class, () -> service.add(req));
    }

    @Test
    void add_AutoVerifywhenSameBank() {
        mockBaseSuccess();
        when(customerClient.getIfscByAccount("ACC1"))
                .thenReturn("HDFC0001");
        when(customerClient.getBankBranch("HDFC0001"))
                .thenReturn(new BeneficiaryServiceImpl.BankBranchInfo("HDFC Bank", "Main"));
        BeneficiaryResponse response = service.add(req);
        assertTrue(response.isVerified());
        verify(repository).save(any(Beneficiary.class));
    }

    @Test
    void add_PendingwhenDifferentBank() {
        mockBaseSuccess();
        req.setIfscCode("ICIC0001");
        when(customerClient.getIfscByAccount("ACC1"))
                .thenReturn("HDFC0001");
        when(customerClient.getBankBranch("HDFC0001"))
                .thenReturn(new BeneficiaryServiceImpl.BankBranchInfo("HDFC Bank", "Main"));
        when(customerClient.getBankBranch("ICIC0001"))
                .thenReturn(new BeneficiaryServiceImpl.BankBranchInfo("ICICI Bank", "Other"));
        BeneficiaryResponse response = service.add(req);
        assertFalse(response.isVerified());
    }

    @Test
    void add_UnknownwhenIfscNull() {
        mockBaseSuccess();
        req.setIfscCode(null);
        when(customerClient.getIfscByAccount("ACC1"))
                .thenReturn(null);
        when(customerClient.getBankBranch(null))
                .thenThrow(mock(FeignException.class));
        BeneficiaryResponse response = service.add(req);
        assertEquals("UNKNOWN", response.getBankName());
    }

    @Test
    void add_UnknownwhenIfscTooShort() {
        mockBaseSuccess();
        req.setIfscCode("AB");
        when(customerClient.getIfscByAccount("ACC1"))
                .thenReturn("AB");
        when(customerClient.getBankBranch("AB"))
                .thenThrow(mock(FeignException.class));
        BeneficiaryResponse response = service.add(req);
        assertEquals("UNKNOWN", response.getBankName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ICIC0001",
            "HDFC0001",
            "SBIN0001",
            "AXIS0001",
            "PUNB0001",
            "UBIN0001",
            "XXXX0001"
    })
    void add_AllIfscPrefixes(String ifsc) {
        mockBaseSuccess();
        req.setIfscCode(ifsc);
        when(customerClient.getIfscByAccount("ACC1"))
                .thenReturn(ifsc);
        when(customerClient.getBankBranch(ifsc))
                .thenThrow(mock(FeignException.class));
        BeneficiaryResponse response = service.add(req);
        assertNotNull(response);
    }

    @Test
    void get_ReturnEntity() {
        when(repository.findById("B1"))
                .thenReturn(Optional.of(Beneficiary.builder().build()));
        assertNotNull(service.get("B1"));
    }

    @Test
    void get_Throw_whenNotFound() {
        when(repository.findById("B1"))
                .thenReturn(Optional.empty());
        assertThrows(TransactionException.class, () -> service.get("B1"));
    }

    @Test
    void adminVerify_Verify() {
        Beneficiary b = Beneficiary.builder().isVerified(false).build();
        when(repository.findById("B1"))
                .thenReturn(Optional.of(b));
        service.adminVerify("B1");
        assertTrue(b.isVerified());
        verify(repository).save(b);
    }

    @Test
    void adminVerify_whenNotFound() {
        when(repository.findById("B1"))
                .thenReturn(Optional.empty());
        assertThrows(TransactionException.class, () -> service.adminVerify("B1"));
    }

    @Test
    void adminVerify_whenAlreadyVerified() {
        Beneficiary b = Beneficiary.builder().isVerified(true).build();
        when(repository.findById("B1"))
                .thenReturn(Optional.of(b));
        assertThrows(TransactionException.class, () -> service.adminVerify("B1"));
    }

    @Test
    void reject_Deactivate() {
        Beneficiary b = Beneficiary.builder().isActive(true).build();
        when(repository.findById("B1"))
                .thenReturn(Optional.of(b));
        service.reject("B1");
        assertFalse(b.isActive());
        verify(repository).save(b);
    }

    @Test
    void reject_whenNotFound() {
        when(repository.findById("B1"))
                .thenReturn(Optional.empty());
        assertThrows(TransactionException.class, () -> service.reject("B1"));
    }

    @Test
    void list_ReturnMapped() {
        when(repository.findByCustomerId("C1"))
                .thenReturn(List.of(Beneficiary.builder().build()));
        assertEquals(1, service.list("C1").size());
    }

    @Test
    void listPendingApprovals_ReturnMapped() {
        when(repository.findByIsVerifiedAndIsActive(false,true))
                .thenReturn(List.of(Beneficiary.builder().build()));
        assertEquals(1, service.listPendingApprovals().size());
    }

    @Test
    void listAll_ReturnMapped() {
        when(repository.findAll())
                .thenReturn(List.of(Beneficiary.builder().build()));
        assertEquals(1, service.listAll().size());
    }
}
