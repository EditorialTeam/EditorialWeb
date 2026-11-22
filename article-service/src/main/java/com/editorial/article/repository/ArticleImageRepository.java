package com.editorial.article.repository;

import com.editorial.article.model.entity.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleImageRepository extends JpaRepository<ArticleImage, Long> {
    Optional<ArticleImage> findByObjectKey(String objectKey);
}
