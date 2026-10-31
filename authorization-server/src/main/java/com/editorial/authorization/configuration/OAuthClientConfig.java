package com.editorial.authorization.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Configuration
public class OAuthClientConfig {
    @Bean
    public RegisteredClientRepository registeredClientRepository(@Value("${OAUTH_CLIENT_SECRETS}") String secretPath) throws IOException {
        Path directory = Path.of(secretPath);
        String clientSecret = Files.readString(directory.resolve("client_secret")).trim();
        RegisteredClient authClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("editorial-auth-service")
                .clientSecret("{bcrypt}" + new BCryptPasswordEncoder().encode(clientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("profiles:create")
                .build();

        return new InMemoryRegisteredClientRepository(authClient);
    }
}
