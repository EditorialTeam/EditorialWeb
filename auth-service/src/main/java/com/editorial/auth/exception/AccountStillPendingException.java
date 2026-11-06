package com.editorial.auth.exception;

public class AccountStillPendingException extends RuntimeException {
    public AccountStillPendingException(String message) {
        super(message);
    }
    public AccountStillPendingException() {super();}
}
