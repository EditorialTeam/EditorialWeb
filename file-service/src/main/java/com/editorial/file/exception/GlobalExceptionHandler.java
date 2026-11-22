package com.editorial.file.exception;

import com.editorial.file.dto.ApiError;
import io.minio.messages.Upload;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.File;
import java.io.FileNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FileStorageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleFileStorageError(FileStorageException e) {
        return new ApiError(
                "FAULTY_FILE",
                "MinIO couldn't process this file"
        );
    }

    @ExceptionHandler(UploadedFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleFileNotFound(UploadedFileNotFoundException e) {
        return new ApiError(
                "FILE_NOT_FOUND",
                "Couldn't find such file"
        );
    }
}
