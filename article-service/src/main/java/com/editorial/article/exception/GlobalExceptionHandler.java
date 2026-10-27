package com.editorial.article.exception;

import com.editorial.article.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleValidationError(MethodArgumentNotValidException e) {
        String code = "Validation error";
        String message = "Request validation failed";
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        return new ApiError(code, message, errors);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiError handleFaultyJsonError(HttpMessageNotReadableException e) {
        String code = "INVALID_JSON";
        String message = "Request body contains invalid JSON";
        Map<String, String> errors = Map.of();

        return new ApiError(code, message, errors);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ArticleNotFoundException.class)
    public ApiError handleNotFoundArticle(ArticleNotFoundException e) {
        String code = "ARTICLE_NOT_FOUND";
        String message = "Couldn't find an article with such ID";
        Map<String, String> errors = Map.of();

        return new ApiError(code, message, errors);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiError handleNotProperRights(AccessDeniedException e) {
        String code = "NOT_ENOUGH_RIGHTS";
        String message = "Not enough rights to edit this article";

        return new ApiError(code, message, null);
    }
}
