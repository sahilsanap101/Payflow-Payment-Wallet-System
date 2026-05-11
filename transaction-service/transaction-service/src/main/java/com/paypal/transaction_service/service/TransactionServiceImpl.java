package com.paypal.transaction_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.paypal.transaction_service.entity.IdempotencyRecord;
import com.paypal.transaction_service.entity.OutboxEvent;
import com.paypal.transaction_service.exception.DuplicateRequestException;
import com.paypal.transaction_service.exception.PaymentProcessingException;
import com.paypal.transaction_service.repository.IdempotencyRecordRepository;
import com.paypal.transaction_service.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paypal.transaction_service.client.WalletClient;
import com.paypal.transaction_service.dto.CreditRequest;
import com.paypal.transaction_service.dto.DebitRequest;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private static final String DEFAULT_CURRENCY = "INR";
    private static final String EVENT_TRANSACTION_SUCCESS = "TRANSACTION_SUCCESS";
    private static final String EVENT_COMPENSATE_SENDER = "COMPENSATE_SENDER";

    private final TransactionRepository repository;
    private final WalletClient walletClient;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final OutboxEventRepository outboxEventRepository;

    public TransactionServiceImpl(
            TransactionRepository repository,
            WalletClient walletClient,
            IdempotencyRecordRepository idempotencyRecordRepository,
            OutboxEventRepository outboxEventRepository
    ) {
        this.repository = repository;
        this.walletClient = walletClient;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    @Transactional
    public Transaction createTransaction(Transaction request, String idempotencyKey) {
        validateRequest(request);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        String requestHash = buildRequestHash(request);
        Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            IdempotencyRecord existing = existingRecord.get();
            if (!existing.getRequestHash().equals(requestHash)) {
                throw new DuplicateRequestException("Idempotency key already used for a different request");
            }
            if (existing.getTransactionId() != null) {
                return repository.findById(existing.getTransactionId())
                        .orElseThrow(() -> new IllegalStateException("Existing idempotent transaction not found"));
            }
        }

        IdempotencyRecord idempotencyRecord = existingRecord.orElseGet(IdempotencyRecord::new);
        idempotencyRecord.setIdempotencyKey(idempotencyKey);
        idempotencyRecord.setRequestHash(requestHash);
        idempotencyRecord.setStatus("PENDING");
        idempotencyRecord = idempotencyRecordRepository.save(idempotencyRecord);

        Transaction transaction = new Transaction();
        transaction.setSenderId(request.getSenderId());
        transaction.setReceiverId(request.getReceiverId());
        transaction.setAmount(request.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("INITIATED");
        transaction = repository.save(transaction);
        idempotencyRecord.setTransactionId(transaction.getId());
        idempotencyRecordRepository.save(idempotencyRecord);

        boolean senderDebited = false;

        try {
            // 1) Debit sender wallet
            walletClient.debit(buildDebitRequest(transaction));
            senderDebited = true;
            transaction.setStatus("DEBITED");
            transaction = repository.save(transaction);

            // 2) Credit receiver wallet
            walletClient.credit(buildCreditRequest(transaction));
            transaction.setStatus("SUCCESS");
            transaction = repository.save(transaction);

            createOutboxEvent(transaction.getId(), EVENT_TRANSACTION_SUCCESS);
            idempotencyRecord.setStatus("SUCCESS");
            idempotencyRecordRepository.save(idempotencyRecord);

            return transaction;

        } catch (Exception ex) {
            log.error("Transaction failed for sender={}, receiver={}, amount={}",
                    request.getSenderId(), request.getReceiverId(), request.getAmount(), ex);

            // Compensation: if sender debit succeeded but credit failed, refund sender.
            if (senderDebited) {
                try {
                    walletClient.credit(buildRefundRequest(transaction));
                    log.warn("Compensation successful for txnId={}", transaction.getId());
                } catch (Exception compensationEx) {
                    log.error("Compensation failed for txnId={}", transaction.getId(), compensationEx);
                    createOutboxEvent(transaction.getId(), EVENT_COMPENSATE_SENDER);
                }
            }

            transaction.setStatus("FAILED");
            repository.save(transaction);
            idempotencyRecord.setStatus("FAILED");
            idempotencyRecordRepository.save(idempotencyRecord);

            throw new PaymentProcessingException("Transaction failed", ex);
        }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    private void validateRequest(Transaction request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getSenderId() == null || request.getReceiverId() == null) {
            throw new IllegalArgumentException("SenderId and ReceiverId are required");
        }
        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be same");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private DebitRequest buildDebitRequest(Transaction transaction) {
        DebitRequest request = new DebitRequest();
        request.setUserId(transaction.getSenderId());
        request.setCurrency(DEFAULT_CURRENCY);
        request.setAmount(toMinorUnits(transaction.getAmount()));
        return request;
    }

    private CreditRequest buildCreditRequest(Transaction transaction) {
        CreditRequest request = new CreditRequest();
        request.setUserId(transaction.getReceiverId());
        request.setCurrency(DEFAULT_CURRENCY);
        request.setAmount(toMinorUnits(transaction.getAmount()));
        return request;
    }

    private CreditRequest buildRefundRequest(Transaction transaction) {
        CreditRequest request = new CreditRequest();
        request.setUserId(transaction.getSenderId());
        request.setCurrency(DEFAULT_CURRENCY);
        request.setAmount(toMinorUnits(transaction.getAmount()));
        return request;
    }

    // Wallet DTO expects minor units as Long (e.g. paise). Convert from BigDecimal safely.
    private Long toMinorUnits(BigDecimal amount) {
        BigDecimal minor = amount
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2);
        return minor.longValueExact();
    }

    private String buildRequestHash(Transaction request) {
        return request.getSenderId()
                + "|" + request.getReceiverId()
                + "|" + request.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void createOutboxEvent(Long transactionId, String eventType) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setTransactionId(transactionId);
        outboxEvent.setEventType(eventType);
        outboxEvent.setStatus("PENDING");
        outboxEventRepository.save(outboxEvent);
    }
}