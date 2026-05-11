package com.paypal.transaction_service.service;

import com.paypal.transaction_service.client.WalletClient;
import com.paypal.transaction_service.dto.CreditRequest;
import com.paypal.transaction_service.entity.OutboxEvent;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.OutboxEventRepository;
import com.paypal.transaction_service.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxPublisherService {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherService.class);
    private static final String EVENT_TRANSACTION_SUCCESS = "TRANSACTION_SUCCESS";
    private static final String EVENT_COMPENSATE_SENDER = "COMPENSATE_SENDER";
    private static final int MAX_RETRIES = 10;
    private static final String DEFAULT_CURRENCY = "INR";

    private final OutboxEventRepository outboxEventRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final WalletClient walletClient;

    public OutboxPublisherService(
            OutboxEventRepository outboxEventRepository,
            TransactionRepository transactionRepository,
            KafkaEventProducer kafkaEventProducer,
            WalletClient walletClient
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaEventProducer = kafkaEventProducer;
        this.walletClient = walletClient;
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop50ByStatusInAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                        List.of("PENDING", "RETRY"),
                        LocalDateTime.now()
                );

        for (OutboxEvent event : events) {
            try {
                Transaction txn = transactionRepository.findById(event.getTransactionId())
                        .orElseThrow(() -> new IllegalStateException("Transaction not found for outbox"));

                if (EVENT_TRANSACTION_SUCCESS.equals(event.getEventType())) {
                    kafkaEventProducer.sendTransactionEventOrThrow(String.valueOf(txn.getId()), txn);
                } else if (EVENT_COMPENSATE_SENDER.equals(event.getEventType())) {
                    CreditRequest request = new CreditRequest();
                    request.setUserId(txn.getSenderId());
                    request.setCurrency(DEFAULT_CURRENCY);
                    request.setAmount(toMinorUnits(txn.getAmount()));
                    walletClient.credit(request);
                }

                event.setStatus("SENT");
                event.setLastError(null);
                outboxEventRepository.save(event);
            } catch (Exception ex) {
                handleFailure(event, ex);
            }
        }
    }

    private void handleFailure(OutboxEvent event, Exception ex) {
        int retryCount = event.getRetryCount() + 1;
        event.setRetryCount(retryCount);
        event.setLastError(ex.getMessage());
        if (retryCount >= MAX_RETRIES) {
            event.setStatus("FAILED");
        } else {
            event.setStatus("RETRY");
            long delaySeconds = Math.min(60, 2L * retryCount);
            event.setNextRetryAt(LocalDateTime.now().plusSeconds(delaySeconds));
        }
        outboxEventRepository.save(event);
        log.error("Outbox processing failed. eventId={}, retryCount={}", event.getId(), retryCount, ex);
    }

    private Long toMinorUnits(BigDecimal amount) {
        BigDecimal minor = amount
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2);
        return minor.longValueExact();
    }
}
