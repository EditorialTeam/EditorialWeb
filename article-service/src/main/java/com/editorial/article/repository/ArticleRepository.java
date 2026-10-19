package com.editorial.article.repository;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
}
