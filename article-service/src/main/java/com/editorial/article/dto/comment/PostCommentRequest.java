package com.editorial.article.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCommentRequest(@NotBlank @Size(max=5000) String content,
                                 Long parentId) {
}
