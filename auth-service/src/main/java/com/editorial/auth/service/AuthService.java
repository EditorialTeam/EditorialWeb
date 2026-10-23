package com.editorial.auth.service;

import com.editorial.auth.dto.LoginRequest;
import com.editorial.auth.dto.LoginResponse;
import com.editorial.auth.dto.RegisterAccountRequest;
import com.editorial.auth.dto.AccountRegisterResponse;
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
    public AuthService(PasswordEncoder passwordEncoder, AccountRepository repository, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.jwtService = jwtService;
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

        return new AccountRegisterResponse(username, email, saved.getCreatedAt());
    }

    @Transactional(readOnly=true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        String password = request.password();

        Account foundAccount = repository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        if (passwordEncoder.matches(password, foundAccount.getPasswordHash())) {
            return new LoginResponse(jwtService.generateAccessToken(foundAccount), "Bearer", 900L);
        } else {
            throw new InvalidCredentialsException();
        }
    }
}
