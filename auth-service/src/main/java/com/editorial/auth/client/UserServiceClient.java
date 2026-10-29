package com.editorial.auth.client;

import com.editorial.auth.dto.UserClientCreateProfileRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {
    private final RestClient restClient;

    public UserServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void createUserProfile(Long accountId, String displayName) {
        restClient.post().uri("/internal/users").body(new UserClientCreateProfileRequest(accountId, displayName))
                .retrieve().toBodilessEntity();
    }
}
