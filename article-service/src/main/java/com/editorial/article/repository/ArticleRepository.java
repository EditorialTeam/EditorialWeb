package com.editorial.article.repository;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
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

    public Optional<Article> updateArticleById(long id, String title, String content) {
        for (Article article : articles) {
            if (article.getId() == id) {
                article.setContent(content);
                article.setTitle(title);
                return Optional.of(article);
            }
        }

        return Optional.empty();
    }

    public void delete(long id) {
        for (int i = 0; i < articles.size(); i++) {
            if (articles.get(i).getId() == id) {
                articles.remove(i);
                return;
            }
        }

        throw new ArticleNotFoundException();
    }

    public Optional<Article> patchArticleById(long id, String title, String content) {
        for (Article article : articles) {
            if (article.getId() == id) {
                if (title != null) article.setTitle(title);
                if (content != null) article.setContent(content);
                return Optional.of(article);
            }
        }

        return Optional.empty();
    }

}
