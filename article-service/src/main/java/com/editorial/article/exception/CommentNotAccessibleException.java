package com.editorial.article.exception;

public class CommentNotAccessibleException extends RuntimeException {
    public CommentNotAccessibleException(String message) {
        super(message);
    }
    public CommentNotAccessibleException() {super();}
}
