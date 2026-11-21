package com.editorial.auth.producer;

import com.editorial.auth.producer.event.ProfileCreationRequested;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class RegistrationEventProducer {
    private final KafkaTemplate<String, ProfileCreationRequested> kafkaTemplate;
    public static final String TOPIC = "PROFILE_CREATION_REQUESTS";
    public static final String EVENT_TYPE = "PROFILE_CREATION";
    public RegistrationEventProducer(KafkaTemplate<String, ProfileCreationRequested> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, ProfileCreationRequested>> sendProfileCreationEvent(ProfileCreationRequested event) {
        return kafkaTemplate.send(TOPIC, event.accountId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) log.error("Failed registration", exception);
                    else log.info("Registration event sent successfully");
                });
    }
}
