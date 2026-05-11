package com.paypal.transaction_service.exception;

import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateRequestException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({UnauthorizedException.class, JwtException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(Exception ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentFailure(PaymentProcessingException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
