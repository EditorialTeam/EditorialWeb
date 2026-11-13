package com.editorial.article.dto.comment;

import java.time.Instant;

public record GetCommentsResponse (Long id,
                                   Long articleId,
                                   Long parentId,
                                   Long authorAccountId,
                                   String authorDisplayName,
                                   String content,
                                   Instant createdAt,
                                   Instant updatedAt,
                                   boolean deleted) {
}
