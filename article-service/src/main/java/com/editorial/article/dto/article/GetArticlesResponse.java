package com.editorial.article.dto.article;

import com.editorial.article.model.ArticleStatus;

import java.time.Instant;

public record GetArticlesResponse(long articleId,
                                  long authorId,
                                  String title,
                                  String content,
                                  String authorDisplayName,
                                  ArticleStatus status,
                                  Instant createdAt,
                                  Instant publishedAt) {

}