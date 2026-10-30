package com.editorial.authorization.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

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
}
