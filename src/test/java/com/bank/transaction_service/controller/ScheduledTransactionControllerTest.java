package com.bank.transaction_service.controller;

import com.bank.transaction_service.entity.ScheduledTransaction;
import com.bank.transaction_service.enums.Frequency;
import com.bank.transaction_service.enums.ScheduledStatus;
import com.bank.transaction_service.repository.ScheduledTransactionRepository;
import com.bank.transaction_service.security.AuthUser;
import com.bank.transaction_service.security.JwtFilter;
import com.bank.transaction_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduledTransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScheduledTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private ScheduledTransactionRepository repository;

    private void setAuth() {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ScheduledTransaction buildSchedule(UUID id) {
        return ScheduledTransaction.builder()
                .id(id)
                .accountNumber("123456")
                .amount(BigDecimal.valueOf(1000))
                .frequency(Frequency.MONTHLY)
                .nextExecutionDate(LocalDate.now())
                .status(ScheduledStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .executionCount(0)
                .build();
    }

    @Test
    void schedule_success() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();
        ScheduledTransaction response = buildSchedule(id);

        when(repository.save(any())).thenReturn(response);

        String json = """
                {
                  "accountNumber": "123456",
                  "amount": 1000,
                  "transactionType": "TRANSFER",
                  "description": "Rent",
                  "frequency": "MONTHLY",
                  "startDate": "2026-01-01",
                  "endDate": "2026-12-01"
                }
                """;

        mockMvc.perform(post("/api/transactions/scheduled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountNumber").value("123456"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Scheduled transaction created successfully"));

        verify(repository).save(any());
    }

    @Test
    void mySchedules_success() throws Exception {
        setAuth();
        when(repository.findByAccountNumber(any()))
                .thenReturn(List.of(buildSchedule(UUID.randomUUID())));

        mockMvc.perform(get("/api/transactions/scheduled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Scheduled transactions fetched successfully"));

        verify(repository).findByAccountNumber(any());
    }

    @Test
    void mySchedules_empty() throws Exception {
        setAuth();
        when(repository.findByAccountNumber(any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/transactions/scheduled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("No scheduled transactions found"));
    }

    @Test
    void pause_success() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();
        ScheduledTransaction schedule = buildSchedule(id);

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        mockMvc.perform(put("/api/transactions/scheduled/{id}/pause", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAUSED"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Scheduled transaction paused successfully"));

        verify(repository).save(any());
    }

    @Test
    void resume_success() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();
        ScheduledTransaction schedule = buildSchedule(id);

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        mockMvc.perform(put("/api/transactions/scheduled/{id}/resume", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Scheduled transaction resumed successfully"));

        verify(repository).save(any());
    }

    @Test
    void cancel_success() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();
        ScheduledTransaction schedule = buildSchedule(id);

        when(repository.findById(id)).thenReturn(Optional.of(schedule));
        when(repository.save(any())).thenReturn(schedule);

        mockMvc.perform(delete("/api/transactions/scheduled/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Scheduled transaction cancelled successfully"));

        verify(repository).save(any());
    }

    @Test
    void pause_notFound() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/transactions/scheduled/{id}/pause", id))
                .andExpect(status().isBadRequest());
    }
    @Test
    void resume_notFound() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/transactions/scheduled/{id}/resume", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Schedule not found"));

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }
    @Test
    void cancel_notFound() throws Exception {
        setAuth();
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/transactions/scheduled/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultInfo.resultMsg")
                        .value("Schedule not found"));

        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    void schedule_unauthorized() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        String json = """
                {
                  "accountNumber": "123456",
                  "amount": 1000,
                  "transactionType": "TRANSFER",
                  "description": "Rent",
                  "frequency": "MONTHLY",
                  "startDate": "2026-01-01",
                  "endDate": "2026-12-01"
                }
                """;

        mockMvc.perform(post("/api/transactions/scheduled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository);
    }
    @Test
    void schedule_notAuthenticated() throws Exception {
        AuthUser user = new AuthUser(UUID.randomUUID(), "ROLE_CUSTOMER");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        auth.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(auth);

        String json = """
            {
              "accountNumber": "123456",
              "amount": 1000,
              "transactionType": "TRANSFER",
              "description": "Rent",
              "frequency": "MONTHLY",
              "startDate": "2026-01-01",
              "endDate": "2026-12-01"
            }
            """;

        mockMvc.perform(post("/api/transactions/scheduled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository);
    }
    @Test
    void schedule_wrongPrincipal() throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "stringUser", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(auth);

        String json = """
            {
              "accountNumber": "123456",
              "amount": 1000,
              "transactionType": "TRANSFER",
              "description": "Rent",
              "frequency": "MONTHLY",
              "startDate": "2026-01-01",
              "endDate": "2026-12-01"
            }
            """;

        mockMvc.perform(post("/api/transactions/scheduled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository);
    }

}
