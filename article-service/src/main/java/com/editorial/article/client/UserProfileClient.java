package com.editorial.article.client;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserProfileClient {
    Map<Long, String> getDisplayNames(Set<Long> authorIds);
}
