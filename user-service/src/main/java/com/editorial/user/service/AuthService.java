package com.editorial.user.service;

import com.editorial.user.dto.RegisterUserRequest;
import com.editorial.user.dto.UserResponse;
import com.editorial.user.exception.UserAlreadyExistsException;
import com.editorial.user.model.User;
import com.editorial.user.model.UserRole;
import com.editorial.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository repository) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String username = request.username();
        String password = request.password();

        if (repository.existsByEmail(email) || repository.existsByUsername(username)) {
            throw new UserAlreadyExistsException();
        }

        String hash = passwordEncoder.encode(password);

        User saved = repository.save(new User(username, email, hash, UserRole.USER));

        return new UserResponse(username, email, saved.getCreatedAt());
    }
}
