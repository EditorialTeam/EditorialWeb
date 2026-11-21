package com.editorial.auth.dto.auth;

public record LoginResponse(String refreshToken, String accessToken, String tokenType, Long expiresIn) { }
