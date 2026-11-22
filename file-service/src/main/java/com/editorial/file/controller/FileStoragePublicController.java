package com.editorial.file.controller;

import com.editorial.file.service.FileStorageService;
import io.minio.GetObjectResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileStoragePublicController {
    private final FileStorageService service;

    public FileStoragePublicController(FileStorageService service) {
        this.service = service;
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
}
