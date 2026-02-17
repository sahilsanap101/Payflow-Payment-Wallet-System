package com.paypal.transaction_service.kafka;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class KafkaEventProducer {

    public static final String TOPIC = "txn-initated";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaEventProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void sendTransactionEvent(String key, String message) {

        System.out.println(
                "Sending to kafka topic: " + TOPIC +
                " with key: " + key +
                " and message: " + message
        );

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(TOPIC, key, message);

        future.thenAccept(result -> {
            RecordMetadata metadata = result.getRecordMetadata();
            System.out.println(
                    "Message sent to topic: " + metadata.topic() +
                    ", partition: " + metadata.partition() +
                    ", offset: " + metadata.offset()
            );
        }).exceptionally(ex -> {
            System.err.println("Failed to send message to Kafka: " + ex.getMessage());
            return null;
        });
    }
}