package com.editorial.article.service;

import com.editorial.article.repository.ArticleRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final ArticleRepository repository;

    public CommentService(ArticleRepository repository) {
        this.repository = repository;
    }


}
