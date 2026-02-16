package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionLimitServiceImplTest {

    @Mock
    private TransactionLimitRepository repository;

    @InjectMocks
    private TransactionLimitServiceImpl service;

    @Test
    void get_exists() {
        TransactionLimit limit = new TransactionLimit("ACC1");

        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.of(limit));

        TransactionLimitResponse response = service.get("ACC1");

        assertNotNull(response);

        verify(repository).findByAccountNumber("ACC1");
        verify(repository, never()).save(any());
    }

    @Test
    void get_createNew() {
        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.empty());

        TransactionLimit saved = new TransactionLimit("ACC1");

        when(repository.save(any()))
                .thenReturn(saved);

        TransactionLimitResponse response = service.get("ACC1");

        assertNotNull(response);
        verify(repository).findByAccountNumber("ACC1");
        verify(repository).save(any());
    }

    @Test
    void update_exists() {
        TransactionLimit limit = new TransactionLimit("ACC1");
        LimitUpdateRequest request = new LimitUpdateRequest();

        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.of(limit));

        TransactionLimitResponse response =
                service.update("ACC1", request);

        assertNotNull(response);
        verify(repository).save(limit);
    }

    @Test
    void update_createNew() {
        LimitUpdateRequest request = new LimitUpdateRequest();
        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.empty());

        TransactionLimit limit = new TransactionLimit("ACC1");

        when(repository.save(any()))
                .thenReturn(limit);

        TransactionLimitResponse response =
                service.update("ACC1", request);

        assertNotNull(response);
        verify(repository).findByAccountNumber("ACC1");
        verify(repository, atLeastOnce()).save(any());
    }
}
