//package com.bank.transaction_service.kafka.event;
//
//import com.bank.transaction_service.dto.client.AccountClient;
//import com.bank.transaction_service.entity.Transaction;
//import com.bank.transaction_service.enums.TransactionStatus;
//import com.bank.transaction_service.repository.TransactionRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TransactionStatusListenerTest {
//
//    @Mock
//    private TransactionRepository repo;
//
//    @Mock
//    private AccountClient accountClient;
//
//    @InjectMocks
//    private TransactionStatusListener listener;
//
//    private Transaction createTransaction(String id) {
//        Transaction tx = new Transaction();
//        tx.setTransactionId(id);
//        tx.setAccountNumber("ACC1");
//        tx.setStatus(TransactionStatus.PENDING);
//        return tx;
//    }
//
//    private TransactionStatusEvent createEvent(String id, String status) {
//        return new TransactionStatusEvent(
//                id,
//                "DEBIT",
//                "ACC1",
//                null,
//                BigDecimal.TEN,
//                status,
//                status.equals("FAILED") ? "Insufficient balance" : null,
//                LocalDateTime.now(),
//                LocalDateTime.now()
//        );
//    }
//
//    @Test
//    void handle_shouldUpdateTransactionAndBalance_whenSuccess() {
//        Transaction tx = createTransaction("TX1");
//
//        when(repo.findByTransactionId("TX1"))
//                .thenReturn(Optional.of(tx));
//
//        when(accountClient.getBalance("ACC1"))
//                .thenReturn(BigDecimal.valueOf(5000));
//
//        listener.handle(createEvent("TX1", "SUCCESS"));
//
//        Assertions.assertEquals(TransactionStatus.SUCCESS, tx.getStatus());
//        Assertions.assertEquals(BigDecimal.valueOf(5000), tx.getCurrentBalance());
//
//        verify(accountClient).getBalance("ACC1");
//        verify(repo).save(tx);
//    }
//
//    @Test
//    void handle_shouldUpdateTransactionWithoutBalance_whenFailed() {
//        Transaction tx = createTransaction("TX2");
//
//        when(repo.findByTransactionId("TX2"))
//                .thenReturn(Optional.of(tx));
//
//        listener.handle(createEvent("TX2", "FAILED"));
//
//        Assertions.assertEquals(TransactionStatus.FAILED, tx.getStatus());
//        Assertions.assertEquals("Insufficient balance", tx.getFailureReason());
//
//        verify(accountClient, never()).getBalance(any());
//        verify(repo).save(tx);
//    }
//
//    @Test
//    void handle_shouldThrowException_whenTransactionNotFound() {
//
//        when(repo.findByTransactionId("TX404"))
//                .thenReturn(Optional.empty());
//
//        Assertions.assertThrows(RuntimeException.class,
//                () -> listener.handle(createEvent("TX404", "SUCCESS")));
//    }
//}
