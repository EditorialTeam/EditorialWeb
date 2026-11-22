package com.editorial.article.controller;

import com.editorial.article.dto.client.UploadFileResponse;
import com.editorial.article.service.ArticleFileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/articles/file")
public class ArticleFileController {
    private final ArticleFileService service;

    public ArticleFileController(ArticleFileService service) {
        this.service = service;
    }

    @PostMapping(value = "/{articleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFileResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam String category,
            @PathVariable long articleId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new UploadFileResponse(
                service.upload(
                Long.parseLong(jwt.getSubject()),
                articleId,
                file,
                category,
                jwt.getClaimAsString("role")
                ))
        );
    }


}
