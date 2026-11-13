package com.editorial.auth.dto.token;

public record RefreshResponse(String accessToken, String refreshToken) { }
