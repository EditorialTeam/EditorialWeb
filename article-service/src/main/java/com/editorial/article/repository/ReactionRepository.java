package com.editorial.article.repository;

import com.editorial.article.model.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByArticleIdAndAuthorAccountId(long articleId, long authorAccountId);

    @Query("""
    SELECT r.reactionType, COUNT(r)
    FROM Reaction r
    WHERE r.article.id = :articleId
    GROUP BY r.reactionType
    """)
    List<Object[]> countReactionsByType(@Param("articleId") long articleId);
}
