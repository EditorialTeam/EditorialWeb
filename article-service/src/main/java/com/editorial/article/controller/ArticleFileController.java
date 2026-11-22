package com.editorial.article.controller;

import com.editorial.article.service.ArticleFileService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class ArticleFileController {
    private final ArticleFileService service;

    public ArticleFileController(ArticleFileService service) {
        this.service = service;
    }
}
