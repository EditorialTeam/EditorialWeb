package com.editorial.user.controller;

import com.editorial.user.dto.client.UploadFileResponse;
import com.editorial.user.dto.user.GetUserProfileResponse;
import com.editorial.user.dto.user.PatchProfileRequest;
import com.editorial.user.dto.user.PatchProfileResponse;
import com.editorial.user.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<GetUserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.getUserProfile(Long.parseLong(Objects.requireNonNull(jwt.getSubject()))));
    }

    @PatchMapping("/me")
    public ResponseEntity<PatchProfileResponse> editProfile(@RequestBody PatchProfileRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(service.editUserProfile(Long.parseLong(Objects.requireNonNull(jwt.getSubject())), request));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFileResponse> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
            ) {
        return ResponseEntity.ok(service.uploadAvatar(Long.parseLong(jwt.getSubject()), file));
    }

}