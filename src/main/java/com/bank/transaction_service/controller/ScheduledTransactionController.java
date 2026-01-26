package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.request.ScheduleTransactionRequest;
import com.bank.transaction_service.dto.response.ScheduleTransactionResponse;
import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.Frequency;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.exception.TransactionException;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import com.bank.transaction_service.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions/scheduled")
@RequiredArgsConstructor
public class ScheduledTransactionController {

    private final ScheduledTransactionRepository repository;

    @PostMapping
    public ScheduleTransactionResponse schedule(
            @RequestBody ScheduleTransactionRequest request
    ) {
        AuthUser user = getAuthUser();

        ScheduledTransaction scheduled = ScheduledTransaction.builder()
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .frequency(Frequency.valueOf(request.getFrequency().toUpperCase()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionDate(request.getStartDate())
                .status(ScheduledStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .executionCount(0)
                .build();

        ScheduledTransaction saved = repository.save(scheduled);

        return ScheduleTransactionResponse.builder()
                .scheduleId(saved.getId().toString())
                .accountNumber(saved.getAccountNumber())
                .frequency(saved.getFrequency().name())
                .nextExecutionDate(saved.getNextExecutionDate())
                .status(saved.getStatus().name())
                .build();
    }

    @GetMapping
    public List<ScheduledTransaction> mySchedules() {
        AuthUser user = getAuthUser();
        // In real scenario, get account number from user context
        return repository.findByAccountNumber(user.getCustomerId().toString());
    }

    @PutMapping("/{scheduleId}/pause")
    public ScheduleTransactionResponse pause(@PathVariable UUID scheduleId) {
        ScheduledTransaction schedule = repository.findById(scheduleId)
                .orElseThrow(() -> TransactionException.badRequest("Schedule not found"));

        schedule.setStatus(ScheduledStatus.PAUSED);
        repository.save(schedule);

        return buildResponse(schedule);
    }

    @PutMapping("/{scheduleId}/resume")
    public ScheduleTransactionResponse resume(@PathVariable UUID scheduleId) {
        ScheduledTransaction schedule = repository.findById(scheduleId)
                .orElseThrow(() -> TransactionException.badRequest("Schedule not found"));

        schedule.setStatus(ScheduledStatus.ACTIVE);
        repository.save(schedule);

        return buildResponse(schedule);
    }

    @DeleteMapping("/{scheduleId}")
    public void cancel(@PathVariable UUID scheduleId) {
        ScheduledTransaction schedule = repository.findById(scheduleId)
                .orElseThrow(() -> TransactionException.badRequest("Schedule not found"));

        schedule.setStatus(ScheduledStatus.CANCELLED);
        repository.save(schedule);
    }

    private ScheduleTransactionResponse buildResponse(ScheduledTransaction s) {
        return ScheduleTransactionResponse.builder()
                .scheduleId(s.getId().toString())
                .accountNumber(s.getAccountNumber())
                .frequency(s.getFrequency().name())
                .nextExecutionDate(s.getNextExecutionDate())
                .status(s.getStatus().name())
                .build();
    }

    private AuthUser getAuthUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw TransactionException.unauthorized("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthUser authUser) {
            return authUser;
        }

        throw TransactionException.unauthorized("Invalid authentication principal");
    }

}