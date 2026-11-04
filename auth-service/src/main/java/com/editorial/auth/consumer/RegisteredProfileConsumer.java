package com.editorial.auth.consumer;

import com.editorial.auth.consumer.event.ProfileRegistered;
import com.editorial.auth.model.Account;
import com.editorial.auth.model.AccountStatus;
import com.editorial.auth.repository.AccountRepository;
import com.editorial.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisteredProfileConsumer {
    private final AccountRepository repository;

    public final static String TOPIC = "PROFILE_CREATED";
    public RegisteredProfileConsumer(AccountRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = TOPIC)
    @Transactional
    public void handleAccountCreationEvent(@Payload ProfileRegistered message  ) {
        Long accountId = message.accountId();

        Account foundAccount = repository.findById(accountId).orElseThrow(IllegalArgumentException::new);

        if (foundAccount.getStatus().name().equals("PENDING")) {
            foundAccount.setStatus(AccountStatus.ACTIVE);
        }

        else {
            log.info("Account already activated");
            return;
        }
    }
}
