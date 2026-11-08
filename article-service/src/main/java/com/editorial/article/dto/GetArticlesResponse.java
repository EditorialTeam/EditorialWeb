package com.editorial.article.dto;

import com.editorial.article.model.ArticleStatus;

import java.time.Instant;

public record GetArticlesResponse(long articleId,
                                  long authorId,
                                  String title,
                                  String content,
                                  ArticleStatus status,
                                  Instant createdAt,
                                  Instant publishedAt) {

}