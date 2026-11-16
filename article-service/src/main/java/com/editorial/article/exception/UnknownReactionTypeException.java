package com.editorial.article.exception;

public class UnknownReactionTypeException extends RuntimeException {
    public UnknownReactionTypeException(String message) {
        super(message);
    }
    public UnknownReactionTypeException() {super();}
}
