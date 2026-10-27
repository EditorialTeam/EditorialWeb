package com.editorial.user.exception;

import com.editorial.user.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserProfileAlreadyExists.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleProfileAlreadyExists(UserProfileAlreadyExists e) {
        String code = "USER_ALREADY_EXISTS";
        String message = "User with this account ID already exists";

        return new ApiError(code, message, null);
    }
}
