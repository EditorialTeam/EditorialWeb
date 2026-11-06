package com.editorial.user.controller;

import com.editorial.user.dto.GetUserProfileResponse;
import com.editorial.user.dto.PatchProfileRequest;
import com.editorial.user.dto.PatchProfileResponse;
import com.editorial.user.service.UserService;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService service;
    public AdminUserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<GetUserProfileResponse>> getProfiles(
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size
    ) {
        Pageable request = PageRequest.of(page, size,
                Sort.by("id").ascending());

        return ResponseEntity.ok(service.getProfiles(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PatchProfileResponse> editProfile(@PathVariable Long accountId, @RequestBody PatchProfileRequest request) {
        return ResponseEntity.ok(service.editUserProfile(accountId, request));
    }
}
