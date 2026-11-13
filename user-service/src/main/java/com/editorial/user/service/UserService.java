package com.editorial.user.service;

import com.editorial.user.dto.client.BatchUsersRequest;
import com.editorial.user.dto.client.PublicUserResponse;
import com.editorial.user.dto.user.GetUserProfileResponse;
import com.editorial.user.dto.user.PatchProfileRequest;
import com.editorial.user.dto.user.PatchProfileResponse;
import com.editorial.user.dto.user.RegisterUserProfileRequest;
import com.editorial.user.exception.UserNotFoundException;
import com.editorial.user.model.OutboxEvent;
import com.editorial.user.model.User;
import com.editorial.user.producer.event.RegisteredProfile;
import com.editorial.user.repository.OutboxEventsRepository;
import com.editorial.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final OutboxEventsRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public UserService (UserRepository repository, OutboxEventsRepository outboxRepository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void add(RegisterUserProfileRequest request) {
        if (repository.existsByAccountId(request.accountId())) return;
        UUID uuid = UUID.randomUUID();

        User savedUser = repository.save(new User(request.accountId(), request.username()));
        RegisteredProfile kafkaPayload = new RegisteredProfile(uuid, savedUser.getAccountId());
        outboxRepository.save(new OutboxEvent(uuid, savedUser.getAccountId(), "PROFILE_CREATED", objectMapper.valueToTree(kafkaPayload)));
        //return savedUser;
    }
    @Transactional(readOnly=true)
    public GetUserProfileResponse getUserProfile(Long accountId) {
        User foundProfile = repository.findByAccountId(accountId).orElseThrow(UserNotFoundException::new);
        return new GetUserProfileResponse(foundProfile.getUsername(), foundProfile.getDisplayName(), foundProfile.getCreatedAt(), foundProfile.getAccountId(), foundProfile.getProfileDescription());
    }

    @Transactional(readOnly = true)
    public Page<GetUserProfileResponse> getProfiles(Pageable request) {
        return repository.findAll(request).map(
                account -> new GetUserProfileResponse(
                        account.getUsername(),
                        account.getDisplayName(),
                        account.getCreatedAt(),
                        account.getAccountId(),
                        account.getProfileDescription())
        );
    }

    @Transactional
    public PatchProfileResponse editUserProfile(Long accountId, PatchProfileRequest request) {
        User foundProfile = repository.findByAccountId(accountId).orElseThrow(UserNotFoundException::new);

        String displayName = request.displayName();
        String profileDescription = request.profileDescription();

        if (displayName != null) {
            foundProfile.setDisplayName(displayName);
        }

        if (profileDescription != null) {
            foundProfile.setProfileDescription(profileDescription);
        }

        User savedUser = repository.save(foundProfile);

        return new PatchProfileResponse(savedUser.getDisplayName(), savedUser.getProfileDescription());
    }

    @Transactional(readOnly = true)
    public List<PublicUserResponse> getProfilesById(BatchUsersRequest request) {
        return repository.findAllByAccountIdIn(request.accountIds()).stream().map(profile ->
                new PublicUserResponse(profile.getAccountId(), profile.getDisplayName())).toList();
    }
}
