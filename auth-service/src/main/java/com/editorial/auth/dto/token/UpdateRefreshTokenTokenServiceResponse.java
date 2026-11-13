package com.editorial.auth.dto.token;

import com.editorial.auth.model.Account;

public record UpdateRefreshTokenTokenServiceResponse(String refreshToken, Account account) {
}
