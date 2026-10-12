package com.ledgerwallet.userservice.infrastructure.persistence.repository;

import com.ledgerwallet.userservice.domain.model.OutboxEvent;
import com.ledgerwallet.userservice.domain.model.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}
