package com.editorial.article.client;

import com.editorial.article.dto.client.UserProfileInfoDto;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserProfileClient {
    Map<Long, UserProfileInfoDto> getUserProfileInfo(Set<Long> authorIds);
}
