package com.editorial.article.controller;

import com.editorial.article.dto.RejectReasonRequest;
import com.editorial.article.model.Article;
import com.editorial.article.model.ModerationDecision;
import com.editorial.article.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/editor/articles")
public class ArticleEditorController {
    private final ArticleService service;

    public ArticleEditorController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<Article>> getPending(@RequestParam(name="page", defaultValue = "0") int page,
                                                            @RequestParam(name="size", defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getPendingArticles(PageRequest.of(page, size, Sort.by("createdAt").ascending().and(Sort.by("id").ascending()))));
    }

    @PostMapping("/{id}/moderate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moderate(@PathVariable long id,
                         @RequestParam ModerationDecision decision,
                         @RequestBody(required = false) RejectReasonRequest request) {
        service.moderateArticle(id, decision, request != null ? request.rejectionReason() : null);
    }

}
