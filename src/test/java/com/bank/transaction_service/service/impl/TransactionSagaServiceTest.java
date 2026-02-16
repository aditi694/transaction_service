package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.dto.client.AccountClient;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.*;
import com.bank.transaction_service.kafka.producer.TransactionStatusProducer;
import com.bank.transaction_service.repository.TransactionSagaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionSagaServiceTest {

    @Mock
    private TransactionSagaRepository sagaRepo;

    @Mock
    private AccountClient accountClient;

    @Mock
    private TransactionStatusProducer statusProducer;

    @InjectMocks
    private TransactionSagaService sagaService;

    private Transaction mockTransaction(TransactionType type) {
        Transaction tx = new Transaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setAccountNumber("ACC123");
        tx.setToAccount("ACC999");
        tx.setAmount(BigDecimal.valueOf(100));
        tx.setCharges(BigDecimal.valueOf(10));
        tx.setTotalAmount(BigDecimal.valueOf(110));
        tx.setTransactionType(type);
        return tx;
    }

    @Test
    void testStart_Success() {
        Transaction tx = mockTransaction(TransactionType.DEBIT);

        when(sagaRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionSaga saga = sagaService.start(tx);

        assertNotNull(saga);
        assertEquals(SagaStatus.IN_PROGRESS, saga.getStatus());
        assertEquals(SagaStep.INITIATED, saga.getCurrentStep());
        verify(sagaRepo).save(any());
    }

    @Test
    void testStart_InvalidAmount() {
        Transaction tx = new Transaction();
        tx.setTransactionId("TX1");
        tx.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalStateException.class, () ->
                sagaService.start(tx)
        );
    }
    @Test
    void testStart_WhenBothAmountsNull_ShouldThrow() {

        Transaction tx = new Transaction();
        tx.setTransactionId("TX101");
        tx.setAccountNumber("ACC123");
        tx.setTransactionType(TransactionType.DEBIT);

        assertThrows(IllegalStateException.class, () ->
                sagaService.start(tx)
        );
    }


    @Test
    void testProcessCredit_Success() {
        Transaction tx = mockTransaction(TransactionType.CREDIT);
        TransactionSaga saga = new TransactionSaga();

        when(accountClient.getBalance(any())).thenReturn(BigDecimal.valueOf(1000));

        sagaService.processCredit(tx, saga);

        verify(accountClient).credit("ACC123", BigDecimal.valueOf(100));
        verify(statusProducer).publishSuccess(tx);
    }

    @Test
    void testProcessCredit_Failure() {
        Transaction tx = mockTransaction(TransactionType.CREDIT);
        TransactionSaga saga = new TransactionSaga();

        doThrow(new RuntimeException("Error"))
                .when(accountClient).credit(any(), any());

        sagaService.processCredit(tx, saga);

        verify(statusProducer).publishFailure(eq(tx), any());
    }

    @Test
    void testProcessDebit_Success() {
        Transaction tx = mockTransaction(TransactionType.DEBIT);
        TransactionSaga saga = new TransactionSaga();

        when(accountClient.getBalance(any())).thenReturn(BigDecimal.valueOf(800));

        sagaService.processDebit(tx, saga);

        verify(accountClient).debit("ACC123", BigDecimal.valueOf(100));
        verify(statusProducer).publishSuccess(tx);
    }

    @Test
    void testProcessDebit_Failure() {

        Transaction tx = mockTransaction(TransactionType.DEBIT);
        TransactionSaga saga = new TransactionSaga();

        doThrow(new RuntimeException("Debit failed"))
                .when(accountClient).debit(any(), any());

        sagaService.processDebit(tx, saga);

        verify(statusProducer).publishFailure(eq(tx), any());
        assertEquals(SagaStatus.FAILED, saga.getStatus());
    }

    @Test
    void testProcessTransfer_Success() {
        Transaction tx = mockTransaction(TransactionType.TRANSFER);
        TransactionSaga saga = new TransactionSaga();

        when(accountClient.getBalance(any())).thenReturn(BigDecimal.valueOf(500));

        sagaService.processTransfer(tx, saga);

        verify(accountClient).transfer(
                "ACC123",
                "ACC999",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10)
        );
        verify(statusProducer).publishSuccess(tx);
    }

    @Test
    void testProcessTransfer_Failure() {
        Transaction tx = mockTransaction(TransactionType.TRANSFER);
        TransactionSaga saga = new TransactionSaga();

        doThrow(new RuntimeException("Transfer failed"))
                .when(accountClient)
                .transfer(any(), any(), any(), any());

        sagaService.processTransfer(tx, saga);

        verify(statusProducer).publishFailure(eq(tx), any());
    }
}
