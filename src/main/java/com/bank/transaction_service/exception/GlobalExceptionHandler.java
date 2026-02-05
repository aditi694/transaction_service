package com.bank.transaction_service.exception;

import com.bank.transaction_service.dto.response.BaseResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<BaseResponse<Void>> handleTransactionException(
            TransactionException ex,
            HttpServletRequest request
    ) {
        logException(ex.getHttpStatus(), request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(BaseResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed at {}: {}", request.getRequestURI(), errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(errors, "INVALID_INPUT"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Malformed JSON at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")
                ? "Request body is required but missing"
                : "Invalid request format. Please check your JSON payload";

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(message, "BAD_REQUEST"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParams(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        log.warn("Missing parameter at {}: {}", request.getRequestURI(), ex.getParameterName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(
                        "Required parameter '" + ex.getParameterName() + "' is missing",
                        "MISSING_REQUIRED_FIELD"
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn("Type mismatch at {}: parameter '{}'", request.getRequestURI(), ex.getName());

        String message = String.format(
                "Invalid value for parameter '%s'. Expected type: %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(message, "INVALID_INPUT"));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<BaseResponse<Void>> handleDateTimeParseException(
            DateTimeParseException ex,
            HttpServletRequest request
    ) {
        log.warn("Date parsing error at {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(
                        "Invalid date format. Please use format: yyyy-MM-dd or yyyy-MM",
                        "INVALID_INPUT"
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ex.getMessage(), "INVALID_INPUT"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed at {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(BaseResponse.error(
                        "Authentication failed. Please login again",
                        "AUTHENTICATION_FAILED"
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.error(
                        "You do not have permission to perform this action",
                        "FORBIDDEN"
                ));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(
                        "The requested endpoint does not exist: " + ex.getRequestURL(),
                        "NOT_FOUND"
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("No resource found at {}", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(
                        "The requested resource does not exist",
                        "NOT_FOUND"
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("Method not supported at {}: {} not allowed", request.getRequestURI(), ex.getMethod());

        String message = String.format(
                "HTTP method %s is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(),
                ex.getSupportedHttpMethods()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(BaseResponse.error(message, "METHOD_NOT_ALLOWED"));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<BaseResponse<Void>> handleFeignException(
            FeignException ex,
            HttpServletRequest request
    ) {
        log.error("Feign client error at {}: Status={}, Message={}",
                request.getRequestURI(), ex.status(), ex.getMessage());

        String message = "External service is temporarily unavailable. Please try again later";
        String errorCode = "EXTERNAL_SERVICE_ERROR";
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        // Handle specific Feign status codes
        if (ex.status() == 404) {
            message = "Requested resource not found in external service";
            errorCode = "NOT_FOUND";
            status = HttpStatus.NOT_FOUND;
        } else if (ex.status() == 400) {
            message = "Invalid request to external service";
            errorCode = "BAD_REQUEST";
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(status)
                .body(BaseResponse.error(message, errorCode));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.error("Illegal state at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(
                        "An unexpected error occurred. Please contact support",
                        "INTERNAL_SERVER_ERROR"
                ));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<BaseResponse<Void>> handleNullPointer(
            NullPointerException ex,
            HttpServletRequest request
    ) {
        log.error("NullPointerException at {}", request.getRequestURI(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(
                        "An internal error occurred. Please try again or contact support",
                        "INTERNAL_SERVER_ERROR"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(
                        "An unexpected error occurred. Please try again later",
                        "INTERNAL_SERVER_ERROR"
                ));
    }

    private void logException(HttpStatus status, String uri, String message) {
        if (status.is5xxServerError()) {
            log.error("Error at {}: {}", uri, message);
        } else {
            log.warn("Error at {}: {}", uri, message);
        }
    }
}