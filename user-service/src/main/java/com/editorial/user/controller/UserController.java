package com.editorial.user.controller;

import com.editorial.user.dto.GetUserProfileResponse;
import com.editorial.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<GetUserProfileResponse> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.getUserProfile(Long.parseLong(Objects.requireNonNull(jwt.getSubject()))));
    }

}
