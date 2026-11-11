package com.editorial.article.repository;

import java.util.Map;
import java.util.Set;

public interface UserProfileClient {
    Map<Long, String> getDisplayNames(Set<Long> accountIds);
}
