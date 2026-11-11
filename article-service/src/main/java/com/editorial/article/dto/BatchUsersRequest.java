package com.editorial.article.dto;

import java.util.Set;

public record BatchUsersRequest(Set<Long> accountIds) {
}
