package com.editorial.article.exception;

public class ReactionNotFoundException extends RuntimeException {
    public ReactionNotFoundException(String message) {
        super(message);
    }
    public ReactionNotFoundException() {super();}
}
