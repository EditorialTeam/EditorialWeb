package com.editorial.user.dto;

import java.time.Instant;

public record GetUserProfileResponse(String username, String displayName, Instant createdAt) { }
