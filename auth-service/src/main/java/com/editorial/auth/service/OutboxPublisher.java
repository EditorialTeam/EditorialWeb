package com.editorial.auth.service;

import com.editorial.auth.model.OutboxEvent;
import com.editorial.auth.producer.RegistrationEventProducer;
import com.editorial.auth.producer.event.ProfileCreationRequested;
import com.editorial.auth.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final RegistrationEventProducer producer;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxRepository outboxRepository, RegistrationEventProducer registrationEventProducer, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
        this.producer = registrationEventProducer;
    }

    @Scheduled(fixedDelay = 1000)
    public void publish() {
        List<OutboxEvent> eventsQueue = outboxRepository.findTop100ByOrderByCreatedAtAscIdAsc();

        for(OutboxEvent event : eventsQueue) {
            try {
                ProfileCreationRequested kafkaPayload = objectMapper.treeToValue(event.getPayload(), ProfileCreationRequested.class);
                log.info("Sending message to Kafka...");
                CompletableFuture<SendResult<String, ProfileCreationRequested>> future = producer.sendProfileCreationEvent(kafkaPayload);
                SendResult<String, ProfileCreationRequested> result = future.join();

                outboxRepository.delete(event);
                log.info("Message sent, event removed");
            } catch (Exception e) {
                log.info("Couldn't send event to Kafka" + e);
            }
        }
    }
}
