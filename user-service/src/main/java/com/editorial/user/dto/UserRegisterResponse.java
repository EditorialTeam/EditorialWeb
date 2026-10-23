package com.editorial.user.dto;

import java.time.Instant;

public record UserRegisterResponse(String username, String email, Instant createdAt) { }
