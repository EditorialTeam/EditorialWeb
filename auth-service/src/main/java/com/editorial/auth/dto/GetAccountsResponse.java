package com.editorial.auth.dto;

import java.time.Instant;

public record GetAccountsResponse(Long accountId, String email, String username, Instant createdAt, String role) {}