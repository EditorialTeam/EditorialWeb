package com.editorial.file.service;

import com.editorial.file.configuration.MinioConfig;
import com.editorial.file.configuration.MinioProperties;
import com.editorial.file.model.enums.FileCategory;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import io.minio.*;

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

    public String upload(MultipartFile file, FileCategory category, long entityId) throws IOException {
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("File is empty or too big");

        String detectedType;

        try (InputStream inputStream = file.getInputStream()) {
            detectedType = tika.detect(inputStream);
        } catch(Exception e) {
            throw new IllegalStateException("Unsupported file", e);
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
            return objectKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
    }

    private String resolveExtension(String contentType) {
        return switch (Objects.requireNonNullElse(contentType, "")) {
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_GIF_VALUE -> ".gif";
            case "image/webp" -> ".webp";
            default -> "";
        };
    }
}
