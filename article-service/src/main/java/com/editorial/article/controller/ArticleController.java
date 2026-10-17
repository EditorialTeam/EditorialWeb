package com.editorial.article.controller;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.PatchArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.model.Article;
import com.editorial.article.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.BindException;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Article> addArticle(@Valid @RequestBody CreateArticleRequest articleRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(articleRequest));
    }
    @PutMapping("/{id}")
    public Article updateArticleById(@Valid @RequestBody UpdateArticleRequest request, @PathVariable long id) {
        return service.updateById(request, id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteArticleById(@PathVariable long id) {
        service.delete(id);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<Article> patchArticleById(@Valid @RequestBody PatchArticleRequest request, @PathVariable long id) {
        return ResponseEntity.ok(service.patchById(request, id));
    }
}
