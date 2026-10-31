package com.editorial.authorization.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
public class JwtConfig {
    @Bean
    public JWKSource<SecurityContext> jwkSource(
            @Value("${APP_JWT_KEYS_DIRECTORY}") String keysDirectory
    ) throws IOException {
        Path directory = Path.of(keysDirectory);

        RSAPublicKey publicKey;
        RSAPrivateKey privateKey;

        try (var input = Files.newInputStream(directory.resolve("private.pem"))) {
            privateKey = RsaKeyConverters.pkcs8().convert(input);
        }

        try (var input = Files.newInputStream(directory.resolve("public.pem"))) {
            publicKey = RsaKeyConverters.x509().convert(input);
        }

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("editorial-oauth-key-1")
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            String clientId = context.getRegisteredClient().getClientId();

            switch(clientId) {
                case "editorial-auth-service" ->
                    context.getClaims().audience(List.of("editorial-user-service"));
                default -> throw new IllegalStateException("Unknown client" + clientId);
            }

        };
    }
}
