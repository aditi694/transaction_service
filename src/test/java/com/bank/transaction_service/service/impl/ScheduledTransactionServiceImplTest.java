package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.request.DebitTransactionRequest;
import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import com.bank.transaction_service.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class ScheduledTransactionServiceImplTest {

    @Mock
    private ScheduledTransactionRepository repository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ScheduledTransactionServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void run_empty() {

        when(repository.findDueTransactions(any()))
                .thenReturn(List.of());

        service.executeScheduledTransactions();

        verify(repository, never()).save(any());
        verify(transactionService, never()).debit(any());
    }

    @Test
    void run_success_noEndDate() {

        ScheduledTransaction st = mock(ScheduledTransaction.class);
        DebitTransactionRequest req = mock(DebitTransactionRequest.class);

        when(repository.findDueTransactions(any()))
                .thenReturn(List.of(st));

        when(st.toDebitRequest()).thenReturn(req);
        when(st.getEndDate()).thenReturn(null);

        service.executeScheduledTransactions();

        verify(transactionService).debit(req);
        verify(st).updateNextExecutionDate();
        verify(repository).save(st);
        verify(st, never()).setStatus(any());
    }

    @Test
    void run_complete_trueBranch() {

        ScheduledTransaction st = mock(ScheduledTransaction.class);
        DebitTransactionRequest req = mock(DebitTransactionRequest.class);

        when(repository.findDueTransactions(any()))
                .thenReturn(List.of(st));

        when(st.toDebitRequest()).thenReturn(req);

        LocalDate end = LocalDate.now();
        LocalDate next = end.plusDays(1);

        when(st.getEndDate()).thenReturn(end);
        when(st.getNextExecutionDate()).thenReturn(next);

        service.executeScheduledTransactions();

        verify(st).setStatus(ScheduledStatus.COMPLETED);
        verify(repository).save(st);
    }

    @Test
    void run_complete_falseBranch() {

        ScheduledTransaction st = mock(ScheduledTransaction.class);
        DebitTransactionRequest req = mock(DebitTransactionRequest.class);

        when(repository.findDueTransactions(any()))
                .thenReturn(List.of(st));

        when(st.toDebitRequest()).thenReturn(req);

        LocalDate end = LocalDate.now();
        LocalDate next = end.minusDays(1);

        when(st.getEndDate()).thenReturn(end);
        when(st.getNextExecutionDate()).thenReturn(next);

        service.executeScheduledTransactions();

        verify(st, never()).setStatus(any());
        verify(repository).save(st);
    }

    @Test
    void run_failure() {

        ScheduledTransaction st = mock(ScheduledTransaction.class);
        DebitTransactionRequest req = mock(DebitTransactionRequest.class);

        when(repository.findDueTransactions(any()))
                .thenReturn(List.of(st));

        when(st.toDebitRequest()).thenReturn(req);

        doThrow(new RuntimeException())
                .when(transactionService)
                .debit(req);

        service.executeScheduledTransactions();

        verify(st).markFailed();
        verify(repository).save(st);
    }
}
