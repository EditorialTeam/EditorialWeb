package com.editorial.file.exception;

public class UploadedFileNotFoundException extends RuntimeException {
    public UploadedFileNotFoundException(String message) {
        super(message);
    }
    public UploadedFileNotFoundException() {super();}
}
