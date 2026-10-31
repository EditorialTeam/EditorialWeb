package com.editorial.auth.client;

import com.editorial.auth.dto.UserClientCreateProfileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
@Component
public class UserServiceClient {
    private final RestClient restClient;

    public UserServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void createUserProfile(Long accountId, String displayName) {
        restClient.post()
                .uri("/internal/users")
                .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("auth-to-users"))
                .attributes(RequestAttributePrincipalResolver.principal("editorial-auth-service"))
                .body(new UserClientCreateProfileRequest(accountId, displayName))
                .retrieve()
                .toBodilessEntity();
    }
}
