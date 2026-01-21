package com.bank.transaction_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private String message;
    private String errorCode;
    private int status;
    private LocalDateTime timestamp;
}
