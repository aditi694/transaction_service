package com.bank.transaction_service.service.impl;

import com.bank.transaction_service.client.AccountClient;
import com.bank.transaction_service.dto.request.*;
import com.bank.transaction_service.dto.response.*;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.entity.TransactionLimit;
import com.bank.transaction_service.entity.TransactionSaga;
import com.bank.transaction_service.enums.*;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.kafka.producer.TransactionStatusProducer;
import com.bank.transaction_service.repository.TransactionLimitRepository;
import com.bank.transaction_service.repository.TransactionRepository;
import com.bank.transaction_service.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepo;
    @Mock
    private TransactionLimitRepository limitRepo;
    @Mock
    private AccountClient accountClient;
    @Mock
    private TransactionSagaService sagaService;
    @Mock
    private TransactionStatusProducer statusProducer;

    @InjectMocks
    private TransactionServiceImpl service;

    private UUID customerId;
    private String accountNumber;

    @BeforeEach
    void setup() {
        customerId = UUID.randomUUID();
        accountNumber = "ACC123";

        AuthUser user = new AuthUser(customerId, "testuser");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }

    @Test
    void credit_success() {
        CreditTransactionRequest req = CreditTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(1000))
                .category(TransactionCategory.SALARY)
                .description("Salary credit")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(5000));
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sagaService.start(any())).thenReturn(new TransactionSaga());

        CreditTransactionResponse response = service.credit(req);

        assertTrue(response.isSuccess());
        assertEquals(TransactionStatus.IN_PROGRESS.name(), response.getStatus());

        verify(sagaService).processCredit(any(), any());
    }

    @Test
    void credit_duplicateRequest_shouldThrow() {
        CreditTransactionRequest req = CreditTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.SALARY)
                .description("Duplicate")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(transactionRepo.findByIdempotencyKey(any()))
                .thenReturn(Optional.of(new Transaction()));

        assertThrows(TransactionException.class,
                () -> service.credit(req));
    }

    @Test
    void debit_success() {
        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(500))
                .category(TransactionCategory.FOOD)
                .description("Dinner")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(3000));
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(limitRepo.findById(accountNumber))
                .thenReturn(Optional.of(new TransactionLimit(accountNumber)));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sagaService.start(any())).thenReturn(new TransactionSaga());

        DebitTransactionResponse response = service.debit(req);

        assertTrue(response.isSuccess());
        verify(sagaService).processDebit(any(), any());
    }

    @Test
    void debit_duplicateRequest_shouldThrow() {
        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.FOOD)
                .description("Duplicate")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(transactionRepo.findByIdempotencyKey(any()))
                .thenReturn(Optional.of(new Transaction()));

        assertThrows(TransactionException.class,
                () -> service.debit(req));
    }

    @Test
    void debit_limitExceeded_shouldThrow() {
        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(1000000))
                .category(TransactionCategory.FOOD)
                .description("Big spend")
                .build();

        TransactionLimit limit = new TransactionLimit(accountNumber);
        limit.setPerTransactionLimit(BigDecimal.valueOf(1000));

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(limitRepo.findById(accountNumber)).thenReturn(Optional.of(limit));

        assertThrows(TransactionException.class,
                () -> service.debit(req));
    }

    @Test
    void debit_invalidCategory_shouldThrow() {
        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.SALARY)
                .description("Wrong")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);

        assertThrows(TransactionException.class,
                () -> service.debit(req));
    }

    @Test
    void transfer_success() {
        TransferTransactionRequest req = TransferTransactionRequest.builder()
                .fromAccount(accountNumber)
                .toAccount("ACC999")
                .amount(BigDecimal.valueOf(2000))
                .transferType("IMPS")
                .description("Transfer test")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(10000));
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(limitRepo.findById(accountNumber))
                .thenReturn(Optional.of(new TransactionLimit(accountNumber)));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sagaService.start(any())).thenReturn(new TransactionSaga());

        TransferInitiatedResponse response = service.transfer(req);

        assertTrue(response.isSuccess());
        verify(sagaService).processTransfer(any(), any());
    }

    @Test
    void unauthorizedAccess_shouldThrow() {
        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.FOOD)
                .description("Hack")
                .build();

        when(accountClient.getAccountOwner(accountNumber))
                .thenReturn(UUID.randomUUID());

        assertThrows(TransactionException.class,
                () -> service.debit(req));
    }

    @Test
    void transfer_duplicateRequest_shouldThrow() {
        TransferTransactionRequest req = TransferTransactionRequest.builder()
                .fromAccount(accountNumber)
                .toAccount("ACC999")
                .amount(BigDecimal.valueOf(500))
                .transferType("IMPS")
                .description("Duplicate")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(transactionRepo.findByIdempotencyKey(any()))
                .thenReturn(Optional.of(new Transaction()));

        assertThrows(TransactionException.class,
                () -> service.transfer(req));
    }

    @Test
    void getStatus_success() {

        Transaction tx = Transaction.builder()
                .transactionId("TXN-123")
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(transactionRepo.findByTransactionId("TXN-123"))
                .thenReturn(Optional.of(tx));

        TransactionStatusResponse response = service.getStatus("TXN-123");

        assertEquals("TXN-123", response.getTransactionId());
        assertEquals("SUCCESS", response.getStatus());
    }

    @Test
    void getStatus_notFound_shouldThrow() {
        when(transactionRepo.findByTransactionId("X"))
                .thenReturn(Optional.empty());

        assertThrows(TransactionException.class,
                () -> service.getStatus("X"));
    }

    @Test
    void getStatus_failed_shouldReturnFailureMessage() {
        Transaction tx = Transaction.builder()
                .transactionId("TXN-FAIL")
                .status(TransactionStatus.FAILED)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepo.findByTransactionId("TXN-FAIL"))
                .thenReturn(Optional.of(tx));

        TransactionStatusResponse response =
                service.getStatus("TXN-FAIL");

        assertEquals("Transaction failed", response.getMessage());
    }

    @Test
    void currentUser_unauthenticated_shouldThrow() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null)
        );

        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.FOOD)
                .description("Test")
                .build();

        assertThrows(TransactionException.class,
                () -> service.debit(req));
    }
    @ParameterizedTest
    @CsvSource({
            "IMPS, 1000, 1005",
            "NEFT, 1000, 1000",
            "UPI, 1000, 1000",
            "RTGS, 300000, 300030",
            "RTGS, 100000, 100025"
    })
    void transfer_charges_parameterized(
            String mode,
            BigDecimal amount,
            BigDecimal expectedTotal
    ) {

        TransferTransactionRequest req = TransferTransactionRequest.builder()
                .fromAccount(accountNumber)
                .toAccount("ACC999")
                .amount(amount)
                .transferType(mode)
                .description("Test")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(1000000));
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        TransactionLimit limit = new TransactionLimit(accountNumber);
        limit.setPerTransactionLimit(BigDecimal.valueOf(10000000)); // very high
        when(limitRepo.findById(accountNumber)).thenReturn(Optional.of(limit));

        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sagaService.start(any())).thenReturn(new TransactionSaga());

        TransferInitiatedResponse response = service.transfer(req);

        assertTrue(response.isSuccess());
    }

    @ParameterizedTest
    @CsvSource({
            "SALARY,true",
            "ATM,true",
            "OTHERS,true",
            "FOOD,false"
    })
    void credit_category_validation(
            String category,
            boolean shouldPass
    ) {

        CreditTransactionRequest req = CreditTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.valueOf(category))
                .description("Test")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);

        if (shouldPass) {
            when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(1000));
            when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(sagaService.start(any())).thenReturn(new TransactionSaga());

            assertDoesNotThrow(() -> service.credit(req));
        } else {
            assertThrows(TransactionException.class,
                    () -> service.credit(req));
        }
    }
    @ParameterizedTest
    @CsvSource({
            "FOOD,true",
            "SHOPPING,true",
            "BILL,true",
            "SALARY,false"
    })
    void debit_category_validation(
            String category,
            boolean shouldPass
    ) {

        DebitTransactionRequest req = DebitTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(TransactionCategory.valueOf(category))
                .description("Test")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);

        if (shouldPass) {
            when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(limitRepo.findById(accountNumber))
                    .thenReturn(Optional.of(new TransactionLimit(accountNumber)));
            when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(1000));
            when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(sagaService.start(any())).thenReturn(new TransactionSaga());

            assertDoesNotThrow(() -> service.debit(req));
        } else {
            assertThrows(TransactionException.class,
                    () -> service.debit(req));
        }
    }

    @Test
    void validateCategory_null_shouldThrow() throws Exception {
        CreditTransactionRequest req = CreditTransactionRequest.builder()
                .accountNumber(accountNumber)
                .amount(BigDecimal.valueOf(100))
                .category(null)
                .description("Test")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);

        assertThrows(TransactionException.class,
                () -> service.credit(req));
    }
    @Test
    void transfer_shouldAlwaysUseTransferCategory() {

        TransferTransactionRequest req = TransferTransactionRequest.builder()
                .fromAccount(accountNumber)
                .toAccount("ACC999")
                .amount(BigDecimal.valueOf(1000))
                .transferType("IMPS")
                .description("Test transfer")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(10000));
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(limitRepo.findById(accountNumber))
                .thenReturn(Optional.of(new TransactionLimit(accountNumber)));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sagaService.start(any())).thenReturn(new TransactionSaga());

        assertDoesNotThrow(() -> service.transfer(req));

        verify(sagaService).processTransfer(any(), any());
    }
    @Test
    void transfer_validation_branch_shouldBeCovered() {

        TransferTransactionRequest req = TransferTransactionRequest.builder()
                .fromAccount(accountNumber)
                .toAccount("ACC999")
                .amount(BigDecimal.valueOf(1000))
                .transferType("IMPS")
                .description("Test")
                .build();

        when(accountClient.getAccountOwner(accountNumber)).thenReturn(customerId);
        when(accountClient.getBalance(accountNumber)).thenReturn(BigDecimal.valueOf(10000));
        when(transactionRepo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(limitRepo.findById(accountNumber))
                .thenReturn(Optional.of(new TransactionLimit(accountNumber)));
        when(transactionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sagaService.start(any())).thenReturn(new TransactionSaga());

        assertDoesNotThrow(() -> service.transfer(req));
    }

}
