package com.paypal.transaction_service.kafka;

import com.paypal.transaction_service.entity.Transaction;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventProducer.class);
    private static final String TOPIC = "txn-initiated";

    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    public KafkaEventProducer(KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransactionEvent(String key, Transaction transaction) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Kafka key cannot be null or blank");
        }
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction payload cannot be null");
        }

        log.info("Publishing transaction event. topic={}, key={}, txnId={}, status={}",
                TOPIC, key, transaction.getId(), transaction.getStatus());

        CompletableFuture<SendResult<String, Transaction>> future =
                kafkaTemplate.send(TOPIC, key, transaction);

        future.thenAccept(result -> {
            RecordMetadata metadata = result.getRecordMetadata();
            log.info("Kafka publish success. topic={}, partition={}, offset={}, key={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);
        }).exceptionally(ex -> {
            log.error("Kafka publish failed. topic={}, key={}, txnId={}",
                    TOPIC, key, transaction.getId(), ex);
            return null;
        });
    }

    public void sendTransactionEventOrThrow(String key, Transaction transaction) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Kafka key cannot be null or blank");
        }
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction payload cannot be null");
        }
        try {
            SendResult<String, Transaction> result = kafkaTemplate.send(TOPIC, key, transaction).get();
            RecordMetadata metadata = result.getRecordMetadata();
            log.info("Outbox Kafka publish success. topic={}, partition={}, offset={}, key={}",
                    metadata.topic(), metadata.partition(), metadata.offset(), key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kafka publish interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Kafka publish failed", e);
        }
    }
}