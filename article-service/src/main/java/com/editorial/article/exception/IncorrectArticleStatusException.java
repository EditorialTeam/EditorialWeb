package com.editorial.article.exception;

public class IncorrectArticleStatusException extends RuntimeException {
    public IncorrectArticleStatusException(String message) {
        super(message);
    }
    public IncorrectArticleStatusException() {}
}
