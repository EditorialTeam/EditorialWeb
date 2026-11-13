package com.editorial.auth.service;

import com.editorial.auth.dto.auth.*;
import com.editorial.auth.dto.token.RefreshRequest;
import com.editorial.auth.dto.token.RefreshResponse;
import com.editorial.auth.dto.token.UpdateRefreshTokenTokenServiceResponse;
import com.editorial.auth.exception.AccountStillPendingException;
import com.editorial.auth.exception.InvalidCredentialsException;
import com.editorial.auth.exception.AccountAlreadyExistsException;
import com.editorial.auth.model.Account;
import com.editorial.auth.model.AccountRole;
import com.editorial.auth.model.AccountStatus;
import com.editorial.auth.model.OutboxEvent;
import com.editorial.auth.producer.event.ProfileCreationRequested;
import com.editorial.auth.repository.AccountRepository;
import com.editorial.auth.repository.OutboxRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository repository;
    private final JwtService jwtService;
    private final RefreshTokenService tokenService;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    public AuthService(
            PasswordEncoder passwordEncoder,
            AccountRepository repository,
            JwtService jwtService,
            RefreshTokenService tokenService,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AccountRegisterResponse register(RegisterAccountRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String username = request.username();
        String password = request.password();

        if (repository.existsByEmail(email) || repository.existsByUsername(username)) {
            throw new AccountAlreadyExistsException("User already exists");
        }

        String hash = passwordEncoder.encode(password);

        Account saved = repository.save(new Account(username, email, hash, AccountRole.USER, AccountStatus.PENDING));

        UUID uuid = UUID.randomUUID();
        ProfileCreationRequested registrationRequest = new ProfileCreationRequested(uuid, saved.getId(), username);

        outboxRepository.save(new OutboxEvent(uuid, saved.getId(), "PROFILE_CREATION_REQUESTED", objectMapper.valueToTree(registrationRequest)));

        return new AccountRegisterResponse(username, email, saved.getCreatedAt());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String password = request.password();

        Account foundAccount = repository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (foundAccount.getStatus() == AccountStatus.PENDING) {
            throw new AccountStillPendingException();
        }

        if (passwordEncoder.matches(password, foundAccount.getPasswordHash())) {
            return new LoginResponse(tokenService.generateRefreshToken(foundAccount), jwtService.generateAccessToken(foundAccount), "Bearer", 900L);
        } else {
            throw new InvalidCredentialsException();
        }
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        UpdateRefreshTokenTokenServiceResponse tokenServiceUpdateToken = tokenService.updateRefreshToken(request.refreshToken());
        Account account = tokenServiceUpdateToken.account();
        String refreshToken = tokenServiceUpdateToken.refreshToken();

        return new RefreshResponse(jwtService.generateAccessToken(account), refreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        tokenService.removeRefreshToken(request.refreshToken());
    }
}
