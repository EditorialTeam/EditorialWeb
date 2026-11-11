package com.editorial.article.client;

import com.editorial.article.dto.BatchUsersRequest;
import com.editorial.article.dto.PublicUserResponse;
import com.editorial.article.repository.UserProfileClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RestUserProfileClient implements UserProfileClient {
    private final RestClient restClient;

    public RestUserProfileClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Map<Long, String> getDisplayNames(Set<Long> accountIds) {
        List<PublicUserResponse> response = restClient
                .post()
                .uri("/api/internal/users/batch")
                .body(new BatchUsersRequest(accountIds))
                .retrieve()
                .body(new ParameterizedTypeReference<List<PublicUserResponse>>() {});


       Map<Long, String> returnMap = new HashMap<>();

       if (response == null) return Map.of();
       for (PublicUserResponse element : response) {
           returnMap.put(element.accountId(), element.displayName());
       }

       return returnMap;
    }
}
