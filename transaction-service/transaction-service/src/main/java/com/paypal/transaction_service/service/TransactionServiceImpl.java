package com.paypal.transaction_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.dto.TransferRequest;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final ObjectMapper objectMapper;

    public TransactionServiceImpl(TransactionRepository repository,
                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Transaction createTransaction(TransferRequest request) {

        Transaction transaction = new Transaction();
        transaction.setSenderName(String.valueOf(request.getSenderId()));
        transaction.setReceiverName(String.valueOf(request.getReceiverId()));
        transaction.setAmount(request.getAmount());
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        return repository.save(transaction);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }
}