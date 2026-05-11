package com.paypal.transaction_service.service;

import com.paypal.transaction_service.client.WalletClient;
import com.paypal.transaction_service.entity.OutboxEvent;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.OutboxEventRepository;
import com.paypal.transaction_service.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private KafkaEventProducer kafkaEventProducer;
    @Mock
    private WalletClient walletClient;

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;

    @Test
    void publishPendingEvents_whenKafkaFails_marksRetry() {
        OutboxEvent event = new OutboxEvent();
        event.setTransactionId(11L);
        event.setEventType("TRANSACTION_SUCCESS");
        event.setStatus("PENDING");
        event.setRetryCount(0);
        event.setNextRetryAt(LocalDateTime.now().minusSeconds(1));

        Transaction txn = new Transaction();
        txn.setId(11L);
        txn.setAmount(new BigDecimal("15.00"));

        when(outboxEventRepository.findTop50ByStatusInAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(any(), any()))
                .thenReturn(List.of(event));
        when(transactionRepository.findById(11L)).thenReturn(Optional.of(txn));
        doThrow(new RuntimeException("kafka down")).when(kafkaEventProducer).sendTransactionEventOrThrow(any(), any());

        outboxPublisherService.publishPendingEvents();

        verify(outboxEventRepository, atLeast(1)).save(any(OutboxEvent.class));
    }
}
