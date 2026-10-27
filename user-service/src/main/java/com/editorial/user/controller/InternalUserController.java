package com.editorial.user.controller;

import com.editorial.user.dto.RegisterUserProfileRequest;
import com.editorial.user.model.User;
import com.editorial.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
    private final UserService service;

    public InternalUserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<User> registerProfile(@RequestBody RegisterUserProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(request));
    }

}
