package com.editorial.article.dto.reaction;

import com.editorial.article.model.enums.ReactionType;

import java.util.Map;

public record GetReactions(Map<ReactionType, Long> reactions) {
}
