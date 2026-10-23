package com.editorial.auth.dto;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> errors) {}