package com.editorial.file.controller;

import com.editorial.file.dto.UploadFileResponse;
import com.editorial.file.model.enums.FileCategory;
import com.editorial.file.service.FileStorageService;
import io.minio.GetObjectResponse;
import org.apache.coyote.Response;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/internal/files")
public class FileStorageInternalController {
    private final FileStorageService service;

    public FileStorageInternalController(FileStorageService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFileResponse> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam FileCategory fileCategory,
            @RequestParam long entityId
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(file, fileCategory, entityId));
    }

    @GetMapping
    public ResponseEntity<InputStreamResource> download(
            @RequestParam String key
    ) {
        GetObjectResponse response = service.download(key);

        String contentType = response.headers().get("Content-Type");
        String contentLength = response.headers().get("Content-Length");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(Long.parseLong(contentLength))
                .body(new InputStreamResource(response));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam String key
    ) {
        service.delete(key);
    }
}
