package com.editorial.article.service;

import com.editorial.article.model.Article;
import com.editorial.article.model.ArticleStatus;
import org.springframework.data.jpa.domain.Specification;

public class ArticleSpecifications {
    public static Specification<Article> hasStatus(ArticleStatus status) {
        if (status == null) {
            return Specification.unrestricted();
        }

        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Article> hasAuthorAccountId(Long accountId) {
        if (accountId == null) {
            return Specification.unrestricted();
        }

        return (root, query, cb) ->
                cb.equal(root.get("authorAccountId"), accountId);
    }
}
