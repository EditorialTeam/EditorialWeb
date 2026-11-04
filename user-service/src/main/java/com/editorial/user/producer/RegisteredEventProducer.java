package com.editorial.user.producer;

import com.editorial.user.producer.event.RegisteredProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
@Slf4j
@Component
public class RegisteredEventProducer {
    private final KafkaTemplate<String, RegisteredProfile> kafkaTemplate;
    public static final String TOPIC = "PROFILE_CREATED";
    public RegisteredEventProducer(KafkaTemplate<String, RegisteredProfile> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, RegisteredProfile>> sendProfileCreated(RegisteredProfile event) {
        return kafkaTemplate.send(TOPIC, event.accountId().toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) log.error("Failed registration", exception);
                    else log.info("Registered event sent successfully");
                });
    }
}
