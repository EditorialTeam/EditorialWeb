package com.editorial.article.dto;

import jakarta.validation.constraints.Size;

public record PatchArticleRequest(@Size(min=1, max=200) String title, @Size(min=1) String content) {
}
