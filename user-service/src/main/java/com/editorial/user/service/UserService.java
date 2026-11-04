package com.editorial.user.service;

import com.editorial.user.dto.RegisterUserProfileRequest;
import com.editorial.user.exception.UserProfileAlreadyExists;
import com.editorial.user.model.OutboxEvent;
import com.editorial.user.model.User;
import com.editorial.user.producer.event.RegisteredProfile;
import com.editorial.user.repository.OutboxEventsRepository;
import com.editorial.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final OutboxEventsRepository outboxRepository;
    private final OutboxPublisher publisher;
    private final ObjectMapper objectMapper;

    public UserService (UserRepository repository, OutboxEventsRepository outboxRepository, OutboxPublisher publisher, ObjectMapper objectMapper) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.publisher = publisher;
    }

    @Transactional
    public void add(RegisterUserProfileRequest request) {
        if (repository.existsByAccountId(request.accountId())) return;
        UUID uuid = UUID.randomUUID();

        User savedUser = repository.save(new User(request.accountId(), request.displayName()));
        RegisteredProfile kafkaPayload = new RegisteredProfile(uuid, savedUser.getAccountId());
        outboxRepository.save(new OutboxEvent(uuid, savedUser.getAccountId(), "PROFILE_CREATED", objectMapper.valueToTree(kafkaPayload)));
        //return savedUser;
    }
}
