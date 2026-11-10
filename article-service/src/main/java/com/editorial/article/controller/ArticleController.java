package com.editorial.article.controller;

import com.editorial.article.dto.*;
import com.editorial.article.model.Article;
import com.editorial.article.model.ArticleStatus;
import com.editorial.article.service.ArticleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService service;
    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<Article>> getArticlesPublished(@Min(0) @RequestParam(name="page", defaultValue = "0") int page,
                                                     @Min(1) @Max(100) @RequestParam(name="size", defaultValue = "20") int size) {
        Pageable request = PageRequest.of(page, size,
                Sort.by("id").ascending());

        return ResponseEntity.ok(service.getAll(request));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<Article>> getMyArticles(@Min(0) @RequestParam(name="page", defaultValue = "0") int page,
                                                       @Min(1) @Max(100) @RequestParam(name="size", defaultValue = "20") int size,
                                                       @AuthenticationPrincipal Jwt jwt) {
        Pageable request = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        return ResponseEntity.ok(service.getMy(request, Long.parseLong(jwt.getSubject())));
    }

    @GetMapping("/{id}")
    public Article getArticleById(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Article> addArticle(@Valid @RequestBody CreateArticleRequest articleRequest,
                                              @AuthenticationPrincipal Jwt jwt) {
        Long accountId = Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(articleRequest, accountId));
    }
    @PutMapping("/{id}")
    public Article updateArticleById(@Valid @RequestBody UpdateArticleRequest request, @PathVariable long id,
                                     @AuthenticationPrincipal Jwt jwt) {
        Long accountId = Long.valueOf(Objects.requireNonNull(jwt.getSubject()));
        String role = jwt.getClaimAsString("role");
        return service.updateById(request, id, accountId, role);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteArticleById(@PathVariable long id, @AuthenticationPrincipal Jwt jwt)
    {
        service.delete(id, Long.parseLong(Objects.requireNonNull(jwt.getSubject())), jwt.getClaimAsString("role"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Article> patchArticleById(@Valid @RequestBody PatchArticleRequest request, @PathVariable long id,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.patchById(request, id, Long.parseLong(Objects.requireNonNull(jwt.getSubject())), jwt.getClaimAsString("role")));
    }

    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitForReview(@PathVariable long id,
                                                @AuthenticationPrincipal Jwt jwt) {
        service.submitForReview(id, Long.parseLong(jwt.getSubject()));
    }

    @PostMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(@PathVariable long id,
                        @AuthenticationPrincipal Jwt jwt) {
        service.archiveArticle(id, Long.parseLong(jwt.getSubject()), jwt.getClaimAsString("role"));
    }
}
