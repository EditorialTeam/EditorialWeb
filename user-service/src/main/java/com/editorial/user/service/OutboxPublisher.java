package com.editorial.user.service;

import com.editorial.user.model.OutboxEvent;
import com.editorial.user.producer.RegisteredEventProducer;
import com.editorial.user.producer.event.RegisteredProfile;
import com.editorial.user.repository.OutboxEventsRepository;
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
    private final OutboxEventsRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final RegisteredEventProducer producer;
    public OutboxPublisher(OutboxEventsRepository repository, ObjectMapper objectMapper, RegisteredEventProducer producer) {
        this.outboxRepository = repository;
        this.objectMapper = objectMapper;
        this.producer = producer;
    }
    @Scheduled(fixedDelay = 1000)
    public void publish() {
        List<OutboxEvent> events = outboxRepository.findTop100ByOrderByCreatedAtAscIdAsc();

        for (OutboxEvent event : events) {
            try {
                CompletableFuture<SendResult<String, RegisteredProfile>> future = producer.sendProfileCreated(objectMapper.treeToValue(event.getPayload(), RegisteredProfile.class));
                SendResult<String, RegisteredProfile> result = future.join();

                outboxRepository.delete(event);
            } catch (Exception e) {
                log.info("Couldn't send event back" + e);
            }
        }
    }
}
