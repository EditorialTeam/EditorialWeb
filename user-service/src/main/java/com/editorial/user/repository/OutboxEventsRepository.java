package com.editorial.user.repository;

import com.editorial.user.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventsRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop100ByOrderByCreatedAtAscIdAsc();
}
