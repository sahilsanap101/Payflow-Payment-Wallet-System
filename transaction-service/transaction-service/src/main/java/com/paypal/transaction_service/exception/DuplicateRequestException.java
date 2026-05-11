package com.paypal.transaction_service.exception;

public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
}
