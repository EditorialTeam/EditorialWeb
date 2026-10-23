package com.editorial.user.dto;

import java.time.Instant;

public record UserResponse(String username, String email, Instant createdAt) { }
