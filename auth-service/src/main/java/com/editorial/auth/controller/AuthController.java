package com.editorial.auth.controller;

import com.editorial.auth.dto.*;
import com.editorial.auth.producer.RegistrationEventProducer;
import com.editorial.auth.producer.event.ProfileCreationRequested;
import com.editorial.auth.service.AuthService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final RegistrationEventProducer testKafka;
    public AuthController(AuthService service, RegistrationEventProducer testKafka) {
        this.service = service;
        this.testKafka = testKafka;
    }

    @PostMapping("/register")
    public ResponseEntity<AccountRegisterResponse> register(@Valid @RequestBody RegisterAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(service.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody LogoutRequest request) {
        service.logout(request);
    }
}
