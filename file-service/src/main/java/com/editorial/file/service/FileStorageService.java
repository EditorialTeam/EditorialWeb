package com.editorial.file.service;

import com.editorial.file.configuration.MinioConfig;
import com.editorial.file.configuration.MinioProperties;
import com.editorial.file.dto.UploadFileResponse;
import com.editorial.file.exception.FileStorageException;
import com.editorial.file.exception.UploadedFileNotFoundException;
import com.editorial.file.model.enums.FileCategory;
import io.minio.errors.ErrorResponseException;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import io.minio.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    //для проверки MIME
    private final Tika tika = new Tika();

    public FileStorageService(MinioProperties minioProperties, MinioClient minioClient) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public UploadFileResponse upload(MultipartFile file, FileCategory category, long entityId) {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) throw new FileStorageException("File is empty or too big");

        String detectedType;

        try (InputStream inputStream = file.getInputStream()) {
            detectedType = tika.detect(inputStream);
        } catch(Exception e) {
            throw new FileStorageException("Unsupported file");
        }

        String extension = resolveExtension(detectedType);
        String objectKey = category.getPrefix() + "/" + entityId + "/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(detectedType)
                            .build()
            );
            return new UploadFileResponse(objectKey);
        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file");
        }
    }

    public GetObjectResponse download(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) throw new IllegalArgumentException("Blank or null key");

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectKey)
                            .build()
            );
        }
        catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) throw new UploadedFileNotFoundException();
            throw new FileStorageException("File couldn't be downloaded");
        } catch (Exception e) {
            throw new FileStorageException();
        }
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) throw new IllegalArgumentException();

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new FileStorageException("Couldn't delete file");
        }
    }

    private String resolveExtension(String contentType) {
        return switch (Objects.requireNonNullElse(contentType, "")) {
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_GIF_VALUE -> ".gif";
            case "image/webp" -> ".webp";
            default -> throw new FileStorageException("Unsupported image type");
        };
    }
}
