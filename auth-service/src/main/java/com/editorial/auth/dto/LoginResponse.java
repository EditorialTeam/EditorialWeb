package com.editorial.auth.dto;

public record LoginResponse(String refreshToken, String accessToken, String tokenType, Long expiresIn) { }
