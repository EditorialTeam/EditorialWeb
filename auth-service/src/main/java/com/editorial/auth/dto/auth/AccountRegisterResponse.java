package com.editorial.auth.dto.auth;

import java.time.Instant;

public record AccountRegisterResponse(String username, String email, Instant createdAt) { }
