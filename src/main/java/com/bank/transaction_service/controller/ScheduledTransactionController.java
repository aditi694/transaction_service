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
import com.bank.transaction_service.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    public ResponseEntity<BaseResponse<ScheduleTransactionResponse>> schedule(
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

        return ResponseEntity.ok(
                BaseResponse.success(
                        buildResponse(saved),
                        "Scheduled transaction created successfully"
                )
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ScheduledTransaction>>> mySchedules() {
        AuthUser user = getAuthUser();

        List<ScheduledTransaction> schedules =
                repository.findByAccountNumber(user.getCustomerId().toString());

        String message = schedules.isEmpty()
                ? "No scheduled transactions found"
                : "Scheduled transactions fetched successfully";

        return ResponseEntity.ok(
                BaseResponse.success(schedules, message)
        );
    }

    @PutMapping("/{scheduleId}/pause")
    public ResponseEntity<BaseResponse<ScheduleTransactionResponse>> pause(
            @PathVariable UUID scheduleId
    ) {
        ScheduledTransaction schedule = repository.findById(scheduleId)
                .orElseThrow(() -> TransactionException.badRequest("Schedule not found"));

        schedule.setStatus(ScheduledStatus.PAUSED);
        repository.save(schedule);

        return ResponseEntity.ok(
                BaseResponse.success(
                        buildResponse(schedule),
                        "Scheduled transaction paused successfully"
                )
        );
    }

    @PutMapping("/{scheduleId}/resume")
    public ResponseEntity<BaseResponse<ScheduleTransactionResponse>> resume(
            @PathVariable UUID scheduleId
    ) {
        ScheduledTransaction schedule = repository.findById(scheduleId)
                .orElseThrow(() -> TransactionException.badRequest("Schedule not found"));

        schedule.setStatus(ScheduledStatus.ACTIVE);
        repository.save(schedule);

        return ResponseEntity.ok(
                BaseResponse.success(
                        buildResponse(schedule),
                        "Scheduled transaction resumed successfully"
                )
        );
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<BaseResponse<Void>> cancel(
            @PathVariable UUID scheduleId
    ) {
        ScheduledTransaction schedule = repository.findById(scheduleId)
                .orElseThrow(() -> TransactionException.badRequest("Schedule not found"));

        schedule.setStatus(ScheduledStatus.CANCELLED);
        repository.save(schedule);

        return ResponseEntity.ok(
                BaseResponse.success(null, "Scheduled transaction cancelled successfully")
        );
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

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof AuthUser)) {

            throw new AccessDeniedException(AppConstants.UNAUTHORIZED);
        }

        return (AuthUser) authentication.getPrincipal();
    }
}
