package com.editorial.article.dto.article;

import com.editorial.article.dto.client.UserProfileInfoDto;
import com.editorial.article.model.enums.ArticleStatus;

import java.time.Instant;
import java.util.Map;

public record GetArticlesResponse(long articleId,
                                  String title,
                                  String content,
                                  String coverObjectKey,
                                  Map<Long, UserProfileInfoDto> authorInfo,
                                  ArticleStatus status,
                                  Instant createdAt,
                                  Instant publishedAt,
                                  String rejectionReason) {

}