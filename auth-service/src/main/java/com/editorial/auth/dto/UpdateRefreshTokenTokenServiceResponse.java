package com.editorial.auth.dto;

import com.editorial.auth.model.Account;

public record UpdateRefreshTokenTokenServiceResponse(String refreshToken, Account account) {
}
