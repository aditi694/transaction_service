package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.LimitUpdateRequest;
import com.bank.transaction_service.dto.response.TransactionLimitResponse;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionLimitServiceImplTest {

    @Mock
    private TransactionLimitRepository repository;

    @InjectMocks
    private TransactionLimitServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void get_exists() {

        TransactionLimit limit = mock(TransactionLimit.class);

        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.of(limit));

        TransactionLimitResponse response = service.get("ACC1");

        assertNotNull(response);
        verify(repository, never()).save(any());
    }

    @Test
    void get_createNew() {

        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.empty());

        TransactionLimit saved = mock(TransactionLimit.class);

        when(repository.save(any()))
                .thenReturn(saved);

        TransactionLimitResponse response = service.get("ACC1");

        assertNotNull(response);
        verify(repository).save(any(TransactionLimit.class));
    }

    @Test
    void update_exists() {

        TransactionLimit limit = mock(TransactionLimit.class);
        LimitUpdateRequest request = mock(LimitUpdateRequest.class);

        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.of(limit));

        TransactionLimitResponse response =
                service.update("ACC1", request);

        assertNotNull(response);
        verify(limit).update(request);
        verify(repository).save(limit);
    }

    @Test
    void update_createNew() {

        LimitUpdateRequest request = mock(LimitUpdateRequest.class);

        when(repository.findByAccountNumber("ACC1"))
                .thenReturn(Optional.empty());

        TransactionLimit newLimit = mock(TransactionLimit.class);

        when(repository.save(any(TransactionLimit.class)))
                .thenReturn(newLimit);

        TransactionLimitResponse response =
                service.update("ACC1", request);

        assertNotNull(response);
        verify(repository, atLeastOnce()).save(any());
    }
}
