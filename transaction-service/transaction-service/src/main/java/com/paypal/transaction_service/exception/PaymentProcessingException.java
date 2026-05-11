package com.paypal.transaction_service.exception;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
