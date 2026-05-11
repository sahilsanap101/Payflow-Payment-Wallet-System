package com.paypal.transaction_service.controller;

import java.util.List;

import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<Transaction> create(
            @Valid @RequestBody Transaction transaction,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication
    ) {
        Long senderId = Long.parseLong(authentication.getName());
        transaction.setSenderId(senderId);
        Transaction saved = service.createTransaction(transaction, idempotencyKey);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(service.getAllTransactions());
    }
}