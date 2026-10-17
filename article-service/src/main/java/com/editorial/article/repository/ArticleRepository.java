package com.editorial.article.repository;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.model.Article;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ArticleRepository {
    private List<Article> articles = new ArrayList<>();
    private long uniqueId = articles.size();

    public ArticleRepository() {

    }

    public Article addArticle(Article article) {
        article.setId(uniqueId);
        articles.add(article);
        uniqueId++;
        return article;
    }

    public List<Article> getArticles() {
        return List.copyOf(articles);
    }

    public Optional<Article> getArticleById(long id) {
        for (Article article : articles) {
            if (article.getId() == id) return Optional.of(article);
        }

        return Optional.empty();
    }

}
