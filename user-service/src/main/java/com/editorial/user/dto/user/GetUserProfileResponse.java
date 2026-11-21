package com.editorial.user.dto.user;

import java.time.Instant;

public record GetUserProfileResponse(String username, String displayName, Instant createdAt, Long accountId, String profileDescription) { }
