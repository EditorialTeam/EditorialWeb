package com.editorial.user.consumer;

import com.editorial.user.consumer.event.ProfileCreationRequested;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class RegistrationEventConsumer {
    public final static String TOPIC = "PROFILE_CREATION_REQUESTS";
    public RegistrationEventConsumer() {}


    @KafkaListener(topics = TOPIC)
    public void receiveRegistrationEvent(@Payload ProfileCreationRequested message  ) {
        log.info("Received message" + message);
    }
}
