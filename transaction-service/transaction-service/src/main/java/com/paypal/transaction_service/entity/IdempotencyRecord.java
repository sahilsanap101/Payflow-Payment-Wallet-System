package com.paypal.transaction_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records", uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String idempotencyKey;

    @Column(nullable = false, length = 200)
    private String requestHash;

    @Column
    private Long transactionId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
