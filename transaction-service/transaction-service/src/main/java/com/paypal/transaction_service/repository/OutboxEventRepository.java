package com.paypal.transaction_service.repository;

import com.paypal.transaction_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop50ByStatusInAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            List<String> statuses,
            LocalDateTime now
    );
}
