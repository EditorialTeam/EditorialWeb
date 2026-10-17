package com.editorial.article.service;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.model.Article;
import com.editorial.article.repository.ArticleRepository;
import org.springframework.expression.spel.ast.OpInc;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {
    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public List<Article> getAll() {
        return repository.getArticles();
    }

    public Optional<Article> findById(long id) {
        return repository.getArticleById(id);
    }

    public Article add(CreateArticleRequest requestBody) {
        return repository.addArticle(new Article(0, requestBody.title(), requestBody.content()));
    }
}
