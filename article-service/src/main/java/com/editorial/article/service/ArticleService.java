package com.editorial.article.service;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.PatchArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import com.editorial.article.repository.ArticleRepository;
import org.springframework.expression.spel.ast.OpInc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {
    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Article> getAll() {
        return repository.findAll();
    }
    @Transactional(readOnly = true)
    public Article findById(long id) {
        Optional<Article> response = repository.findById(id);
        return response.orElseThrow(ArticleNotFoundException::new);
    }
    @Transactional
    public Article add(CreateArticleRequest requestBody) {
        return repository.save(new Article(requestBody.title(), requestBody.content()));
    }

    @Transactional
    public Article updateById(UpdateArticleRequest request, long id) {
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);
        response.setTitle(request.title());
        response.setContent(request.content());
        return response;
    }
    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }
    @Transactional
    public Article patchById(PatchArticleRequest request, long id) {
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);
        if (request.title() != null) response.setTitle(request.title());
        if (request.content() != null) response.setContent(request.content());
        return response;
    }
}
