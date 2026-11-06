package com.editorial.auth.exception;

import com.editorial.auth.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleAccountAlreadyExistsError(AccountAlreadyExistsException e) {
        return new ApiError("CONFLICT", "User Already exists", null); //tbd
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidCredentials(InvalidCredentialsException e) {
        return new ApiError("INVALID_CREDENTIALS", "Invalid email or password", null);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleRefreshTokenExpired(InvalidRefreshTokenException e) {
        return new ApiError("INVALID_REFRESH_TOKEN", "Refresh token is invalid, need to re-login", null);
    }

    @ExceptionHandler(AccountStillPendingException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleAccountStillPending(AccountStillPendingException e) {
        return new ApiError("ACCOUNT_STILL_PENDING", "Couldn't log you in because user profile hasn't been created yet", null);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAccountNotFound(AccountNotFoundException e) {
        return new ApiError("ACCOUNT_NOT_FOUND", "Couldn't find such account", null);
    }
}
