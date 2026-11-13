package com.editorial.article.dto.client;

import java.util.Set;

public record BatchUsersRequest(Set<Long> accountIds) {
}
