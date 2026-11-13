package com.editorial.article.client;

import com.editorial.article.dto.client.BatchUsersRequest;
import com.editorial.article.dto.client.PublicUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.*;
@Slf4j
@Component
public class RestUserProfileClient implements UserProfileClient {
    private final RestClient restClient;

    public RestUserProfileClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Map<Long, String> getDisplayNames(Set<Long> accountIds) {
        if (accountIds.isEmpty()) return Map.of();

        List<PublicUserResponse> response = null;
        try {
            response = restClient
                    .post()
                    .uri("/api/internal/users/batch")
                    .body(new BatchUsersRequest(accountIds))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PublicUserResponse>>() {});
        } catch (RestClientException e) {
            log.warn("Couldn't fetch from user-service");
            return Map.of();
        }


        Map<Long, String> returnMap = new HashMap<>();

       if (response == null) return Map.of();
       for (PublicUserResponse element : response) {
           returnMap.put(element.accountId(), element.displayName());
       }

       return returnMap;
    }
}
