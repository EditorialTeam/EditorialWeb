package com.editorial.auth.model;

import com.editorial.auth.producer.event.ProfileCreationRequested;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;

    @Column(name="aggregate_id", nullable = false)
    private long aggregateId;

    @Column(name="event_type", nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable=false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(nullable=false, name="created_at")
    private Instant createdAt;


    @PrePersist
    protected void onCreated() {
        createdAt = Instant.now();
    }

    protected OutboxEvent() {}

    public OutboxEvent(UUID id, long aggregateId, String eventType, JsonNode payload) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public long getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
