package com.paypal.transaction_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CaptureRequest {

    @NotBlank(message = "holdReference is required")
    @Size(max = 100, message = "holdReference is too long")
    private String holdReference;

    public CaptureRequest() {
    }

    public CaptureRequest(String holdReference) {
        this.holdReference = holdReference;
    }

    public String getHoldReference() {
        return holdReference;
    }

    public void setHoldReference(String holdReference) {
        this.holdReference = holdReference;
    }
}