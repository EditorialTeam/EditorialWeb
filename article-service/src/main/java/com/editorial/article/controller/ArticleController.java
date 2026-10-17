package com.editorial.article.controller;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.model.Article;
import com.editorial.article.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
        Optional<Article> findArticle = service.findById(id);
        if (findArticle.isPresent()) {
            return findArticle.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found");
        }

    }

    @PostMapping
    public ResponseEntity<Article> addArticle(@Valid @RequestBody CreateArticleRequest articleRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(articleRequest));
    }
}
