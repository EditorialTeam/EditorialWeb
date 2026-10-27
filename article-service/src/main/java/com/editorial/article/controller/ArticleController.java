package com.editorial.article.controller;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.PatchArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.model.Article;
import com.editorial.article.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService service;
    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public List<Article> getArticles() {
        return service.getAll();
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
        service.delete(id, Long.parseLong(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Article> patchArticleById(@Valid @RequestBody PatchArticleRequest request, @PathVariable long id,
                                                    @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.patchById(request, id, Long.parseLong(jwt.getSubject()), jwt.getClaimAsString("role")));
    }
}
