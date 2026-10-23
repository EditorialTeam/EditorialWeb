package com.editorial.auth.dto;

public record LoginResponse(String accessToken, String tokenType, Long expiresIn) { }
