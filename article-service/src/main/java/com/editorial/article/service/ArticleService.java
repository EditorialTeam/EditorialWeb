package com.editorial.article.service;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.PatchArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
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

    public Article findById(long id) {
        Optional<Article> response = repository.getArticleById(id);
        return response.orElseThrow(ArticleNotFoundException::new);
    }

    public Article add(CreateArticleRequest requestBody) {
        return repository.addArticle(new Article(requestBody.title(), requestBody.content()));
    }

    public Article updateById(UpdateArticleRequest request, long id) {
        Optional<Article> response = repository.updateArticleById(id, request.title(), request.content());
        return response.orElseThrow(ArticleNotFoundException::new);
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public Article patchById(PatchArticleRequest request, long id) {
        Optional<Article> response = repository.patchArticleById(id, request.title(), request.content());
        return response.orElseThrow(ArticleNotFoundException::new);
    }
}
