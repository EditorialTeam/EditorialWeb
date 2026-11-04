package com.editorial.user.consumer;

import com.editorial.user.consumer.event.ProfileCreationRequested;
import com.editorial.user.dto.RegisterUserProfileRequest;
import com.editorial.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class RegistrationEventConsumer {
    private final UserService service;

    public final static String TOPIC = "PROFILE_CREATION_REQUESTS";
    public RegistrationEventConsumer(UserService service) {
        this.service = service;
    }

    @KafkaListener(topics = TOPIC)
    public void receiveRegistrationEvent(@Payload ProfileCreationRequested message  ) {
        log.info("Trying to register user profile...");
        service.add(new RegisterUserProfileRequest(message.accountId(), message.displayName()));
        log.info("Received message: {log_info}" + message);
    }
}
