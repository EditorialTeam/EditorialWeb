package com.editorial.auth.dto;

import java.time.Instant;

public record AccountRegisterResponse(String username, String email, Instant createdAt) { }
