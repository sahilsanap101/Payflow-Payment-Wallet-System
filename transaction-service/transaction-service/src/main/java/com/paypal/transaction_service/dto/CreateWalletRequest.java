package com.paypal.transaction_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateWalletRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be 3-letter code")
    private String currency;

    public CreateWalletRequest() {
    }

    public CreateWalletRequest(Long userId, String currency) {
        this.userId = userId;
        this.currency = currency;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}