package com.editorial.user.service;

import com.editorial.user.dto.RegisterUserProfileRequest;
import com.editorial.user.exception.UserProfileAlreadyExists;
import com.editorial.user.model.User;
import com.editorial.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService (UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public User add(RegisterUserProfileRequest request) {
        if (repository.existsByAccountId(request.accountId())) throw new UserProfileAlreadyExists();
        return repository.save(new User(request.accountId(), request.displayName()));
    }
}
