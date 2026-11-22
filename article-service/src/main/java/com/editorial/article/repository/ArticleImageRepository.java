package com.editorial.article.repository;

import com.editorial.article.model.entity.Article;
import com.editorial.article.model.entity.ArticleImage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleImageRepository extends JpaRepository<ArticleImage, Long> {
    Optional<ArticleImage> findByObjectKey(String objectKey);
    List<ArticleImage> findAllByArticleId(long articleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ArticleImage a WHERE a.id = :id")
    Optional<ArticleImage> findByIdForUpdate(@Param("id") long id);
}
