package com.paypal.transaction_service.service;

import com.paypal.transaction_service.client.WalletClient;
import com.paypal.transaction_service.entity.IdempotencyRecord;
import com.paypal.transaction_service.entity.OutboxEvent;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.exception.DuplicateRequestException;
import com.paypal.transaction_service.exception.PaymentProcessingException;
import com.paypal.transaction_service.repository.IdempotencyRecordRepository;
import com.paypal.transaction_service.repository.OutboxEventRepository;
import com.paypal.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private WalletClient walletClient;
    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction request;

    @BeforeEach
    void setUp() {
        request = new Transaction();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(new BigDecimal("10.50"));
    }

    @Test
    void createTransaction_success_persistsOutbox() {
        when(idempotencyRecordRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(idempotencyRecordRepository.save(any(IdempotencyRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction txn = inv.getArgument(0);
            if (txn.getId() == null) {
                txn.setId(99L);
            }
            return txn;
        });

        Transaction result = transactionService.createTransaction(request, "k1");

        assertEquals("SUCCESS", result.getStatus());
        verify(walletClient).debit(any());
        verify(walletClient).credit(any());
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void createTransaction_duplicateKeyDifferentPayload_throwsConflict() {
        IdempotencyRecord existing = new IdempotencyRecord();
        existing.setIdempotencyKey("k1");
        existing.setRequestHash("different");
        when(idempotencyRecordRepository.findByIdempotencyKey("k1")).thenReturn(Optional.of(existing));

        assertThrows(DuplicateRequestException.class, () -> transactionService.createTransaction(request, "k1"));
    }

    @Test
    void createTransaction_duplicateKeySamePayload_returnsExistingTransaction() {
        IdempotencyRecord existing = new IdempotencyRecord();
        existing.setIdempotencyKey("k1");
        existing.setRequestHash("1|2|10.50");
        existing.setTransactionId(7L);
        when(idempotencyRecordRepository.findByIdempotencyKey("k1")).thenReturn(Optional.of(existing));
        Transaction existingTxn = new Transaction();
        existingTxn.setId(7L);
        existingTxn.setStatus("SUCCESS");
        when(transactionRepository.findById(7L)).thenReturn(Optional.of(existingTxn));

        Transaction result = transactionService.createTransaction(request, "k1");

        assertEquals(7L, result.getId());
        verify(walletClient, never()).debit(any());
    }

    @Test
    void createTransaction_whenCreditFails_marksFailedAndThrows() {
        when(idempotencyRecordRepository.findByIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(idempotencyRecordRepository.save(any(IdempotencyRecord.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction txn = inv.getArgument(0);
            if (txn.getId() == null) {
                txn.setId(100L);
            }
            return txn;
        });
        doThrow(new RuntimeException("credit failed")).when(walletClient).credit(any());

        assertThrows(PaymentProcessingException.class, () -> transactionService.createTransaction(request, "k1"));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeastOnce()).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(txn -> "FAILED".equals(txn.getStatus())));
    }
}
