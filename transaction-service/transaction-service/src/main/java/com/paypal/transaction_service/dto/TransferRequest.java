package com.paypal.transaction_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferRequest {

    // senderId should normally come from authenticated context/header
    private Long senderId;

    @NotNull(message = "receiverId is required")
    private Long receiverId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    public TransferRequest() {
    }

    public TransferRequest(Long senderId, Long receiverId, BigDecimal amount) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}