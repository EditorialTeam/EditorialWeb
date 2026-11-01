package com.editorial.auth.service;

import com.editorial.auth.client.UserServiceClient;
import com.editorial.auth.dto.*;
import com.editorial.auth.exception.InvalidCredentialsException;
import com.editorial.auth.exception.AccountAlreadyExistsException;
import com.editorial.auth.model.Account;
import com.editorial.auth.model.AccountRole;
import com.editorial.auth.repository.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository repository;
    private final JwtService jwtService;
    private final UserServiceClient serviceClient;
    private final RefreshTokenService tokenService;
    public AuthService(PasswordEncoder passwordEncoder, AccountRepository repository, JwtService jwtService, UserServiceClient serviceClient, RefreshTokenService tokenService) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.jwtService = jwtService;
        this.serviceClient = serviceClient;
        this.tokenService = tokenService;
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

        Account saved = repository.save(new Account(username, email, hash, AccountRole.USER));

        serviceClient.createUserProfile(saved.getId(), saved.getUsername());

        return new AccountRegisterResponse(username, email, saved.getCreatedAt());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String password = request.password();

        Account foundAccount = repository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

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
        tokenService.removeRefreshToken(request.oldRefreshToken());
    }
}
