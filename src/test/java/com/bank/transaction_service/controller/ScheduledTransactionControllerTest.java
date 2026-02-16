package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.ScheduleTransactionRequest;
import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.dto.response.ScheduleTransactionResponse;
import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.Frequency;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTransactionControllerTest {

    @Mock
    private ScheduledTransactionRepository repository;

    @InjectMocks
    private ScheduledTransactionController controller;

    private UUID customerId = UUID.randomUUID();

    private void mockAuthenticatedUser() {
        AuthUser user = mock(AuthUser.class);
        lenient().when(user.getCustomerId()).thenReturn(customerId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    private ScheduledTransaction validSchedule(UUID id, ScheduledStatus status) {
        return ScheduledTransaction.builder()
                .id(id)
                .accountNumber(customerId.toString())
                .amount(BigDecimal.TEN)
                .transactionType("DEBIT")
                .frequency(Frequency.DAILY)
                .startDate(LocalDate.now())
                .nextExecutionDate(LocalDate.now())
                .status(status)
                .build();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testSchedule_Success() {
        mockAuthenticatedUser();
        ScheduleTransactionRequest request =
                new ScheduleTransactionRequest(
                        customerId.toString(),
                        BigDecimal.TEN,
                        "DEBIT",
                        "DAILY",
                        LocalDate.now(),
                        null,
                        "test"
                );

        ScheduledTransaction saved =
                validSchedule(UUID.randomUUID(), ScheduledStatus.ACTIVE);

        when(repository.save(any())).thenReturn(saved);

        ResponseEntity<BaseResponse<ScheduleTransactionResponse>> response =
                controller.schedule(request);

        Assertions.assertEquals("Scheduled transaction created successfully",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testSchedule_Unauthorized() {
        SecurityContextHolder.clearContext();
        Assertions.assertThrows(AccessDeniedException.class,
                () -> controller.schedule(new ScheduleTransactionRequest()));
    }

    @Test
    void testMySchedules_WithData() {
        mockAuthenticatedUser();
        when(repository.findByAccountNumber(customerId.toString()))
                .thenReturn(List.of(validSchedule(UUID.randomUUID(), ScheduledStatus.ACTIVE)));

        ResponseEntity<BaseResponse<List<ScheduledTransaction>>> response =
                controller.mySchedules();

        Assertions.assertEquals("Scheduled transactions fetched successfully",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testMySchedules_Empty() {
        mockAuthenticatedUser();
        when(repository.findByAccountNumber(customerId.toString()))
                .thenReturn(List.of());

        ResponseEntity<BaseResponse<List<ScheduledTransaction>>> response =
                controller.mySchedules();

        Assertions.assertEquals("No scheduled transactions found",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testPause_Success() {
        UUID id = UUID.randomUUID();
        ScheduledTransaction schedule = validSchedule(id, ScheduledStatus.ACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        ResponseEntity<BaseResponse<ScheduleTransactionResponse>> response =
                controller.pause(id);

        Assertions.assertEquals("Scheduled transaction paused successfully",
                response.getBody().getResultInfo().getResultMsg());
    }

    @Test
    void testResume_Success() {
        UUID id = UUID.randomUUID();
        ScheduledTransaction schedule = validSchedule(id, ScheduledStatus.PAUSED);

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        ResponseEntity<BaseResponse<ScheduleTransactionResponse>> response =
                controller.resume(id);

        Assertions.assertEquals("Scheduled transaction resumed successfully",
                response.getBody().getResultInfo().getResultMsg());
    }
    @Test
    void testResume_NotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(TransactionException.class,
                () -> controller.resume(id));
    }

    @Test
    void testCancel_Success() {
        UUID id = UUID.randomUUID();
        ScheduledTransaction schedule = validSchedule(id, ScheduledStatus.ACTIVE);

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        ResponseEntity<BaseResponse<Void>> response =
                controller.cancel(id);

        Assertions.assertEquals("Scheduled transaction cancelled successfully",
                response.getBody().getResultInfo().getResultMsg());
    }
    @Test
    void testCancel_NotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(TransactionException.class,
                () -> controller.cancel(id));
    }

    @Test
    void testPause_NotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(TransactionException.class, () -> controller.pause(id));
    }

    @Test
    void testUnauthorized_NotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class,
                () -> controller.schedule(new ScheduleTransactionRequest()));
    }

    @Test
    void testUnauthorized_InvalidPrincipal() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("invalid");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        Assertions.assertThrows(AccessDeniedException.class,
                () -> controller.schedule(new ScheduleTransactionRequest()));
    }

}
