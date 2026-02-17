package com.paypal.transaction_service.service;

import java.util.List;

import com.paypal.transaction_service.dto.TransferRequest;
import com.paypal.transaction_service.entity.Transaction;

public interface TransactionService {

    Transaction createTransaction(TransferRequest request);

    List<Transaction> getAllTransactions();
}