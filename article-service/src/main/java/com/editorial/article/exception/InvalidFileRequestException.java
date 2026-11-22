package com.editorial.article.exception;

public class InvalidFileRequestException extends RuntimeException {
    public InvalidFileRequestException(String message) {
        super(message);
    }
}
