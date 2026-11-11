package com.editorial.user.controller;

import com.editorial.user.dto.BatchUsersRequest;
import com.editorial.user.dto.PublicUserResponse;
import com.editorial.user.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {
    private final UserService service;

    public InternalUserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/batch")
    public List<PublicUserResponse> getUserProfiles(@RequestBody BatchUsersRequest request) {
        return service.getProfilesById(request);
    }
}
