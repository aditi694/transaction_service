package com.bank.transaction_service.controller;

import com.bank.transaction_service.dto.response.BaseResponse;
import com.bank.transaction_service.entity.Transaction;
import com.bank.transaction_service.enums.TransactionType;
import com.bank.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionInternalControllerTest {

    @Mock
    private TransactionRepository repo;

    @InjectMocks
    private TransactionInternalController controller;

    @Test
    public void testTotalDebit() {
        UUID customerId = UUID.randomUUID();

        Transaction t1 = new Transaction();
        t1.setTransactionType(TransactionType.DEBIT);
        t1.setAmount(BigDecimal.valueOf(100));

        Transaction t2 = new Transaction();
        t2.setTransactionType(TransactionType.TRANSFER);
        t2.setAmount(BigDecimal.valueOf(200));

        Transaction t3 = new Transaction();
        t3.setTransactionType(TransactionType.CREDIT);
        t3.setAmount(BigDecimal.valueOf(500));

        when(repo.findByCustomerId(customerId))
                .thenReturn(List.of(t1, t2, t3));

        ResponseEntity<BaseResponse<Double>> response =
                controller.totalDebit(customerId);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(300.0, response.getBody().getData());
        Assertions.assertEquals(
                "Total debit calculated successfully",
                response.getBody().getResultInfo().getResultMsg()
        );
    }

    @Test
    public void testTotalDebit_EmptyList() {
        UUID customerId = UUID.randomUUID();

        when(repo.findByCustomerId(customerId))
                .thenReturn(List.of());

        ResponseEntity<BaseResponse<Double>> response =
                controller.totalDebit(customerId);

        Assertions.assertEquals(0.0, response.getBody().getData());
        Assertions.assertEquals(
                "Total debit calculated successfully",
                response.getBody().getResultInfo().getResultMsg()
        );
    }
}
