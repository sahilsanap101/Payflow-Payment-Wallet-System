package com.paypal.transaction_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paypal.transaction_service.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}