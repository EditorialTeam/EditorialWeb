package com.editorial.user.dto;

import java.time.Instant;

public record GetUserProfileResponse(String displayName, Instant createdAt) { }
