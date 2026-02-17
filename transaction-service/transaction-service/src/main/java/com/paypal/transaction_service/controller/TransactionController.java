package com.paypal.transaction_service.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.paypal.transaction_service.dto.TransferRequest;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    public ResponseEntity<Transaction> create(
            @Valid @RequestBody TransferRequest request
    ) {
        Transaction created = transactionService.createTransaction(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public List<Transaction> getAll() {
        return transactionService.getAllTransactions();
    }
}