package com.paypal.transaction_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DebitRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 3, message = "currency must be 3-letter code")
    private String currency;

    @NotNull(message = "amount is required")
    @Min(value = 1, message = "amount must be greater than 0")
    private Long amount;

    public DebitRequest() {
    }

    public DebitRequest(Long userId, String currency, Long amount) {
        this.userId = userId;
        this.currency = currency;
        this.amount = amount;
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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}