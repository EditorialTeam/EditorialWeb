package com.editorial.auth.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class OAuthClientConfig {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${OAUTH_CLIENT_SECRETS}") String secretPath,
            @Value("${AUTH_SERVER_URL}") String authServerUrl
    ) throws IOException {
        String clientSecret = Files.readString(Path.of(secretPath).resolve("client_secret")).trim();

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("auth-to-users")
                .clientId("editorial-auth-service")
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("profiles:create")
                .tokenUri(authServerUrl + "/oauth2/token")
                .build();

        return new InMemoryClientRegistrationRepository(clientRegistration);

    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository registrations
    ) {
        OAuth2AuthorizedClientService clientService =
                new InMemoryOAuth2AuthorizedClientService(registrations);

        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                registrations,
                clientService
        );
    }
}
