package com.editorial.auth.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {
    @Bean
    public JwtEncoder jwtEncoder(
            @Value("file:${APP_JWT_KEYS_DIRECTORY}/private.pem") Resource privateResource,
            @Value("file:${APP_JWT_KEYS_DIRECTORY}/public.pem") Resource publicResource
            ) throws IOException {
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;

        try (InputStream input = privateResource.getInputStream()) {
            privateKey = RsaKeyConverters.pkcs8().convert(input);
        }

        try (InputStream input = publicResource.getInputStream()) {
            publicKey = RsaKeyConverters.x509().convert(input);
        }

        return NimbusJwtEncoder.withKeyPair(publicKey, privateKey).jwkPostProcessor(jwk -> jwk.keyID("editorial-key-1")).build();
    }

    @Bean
    public RSAPublicKey rsaPublicKey(@Value("file:${APP_JWT_KEYS_DIRECTORY}/public.pem") Resource publicResource) throws IOException {
        RSAPublicKey rsaPublicKey;
        try (InputStream input = publicResource.getInputStream()) {
            rsaPublicKey = RsaKeyConverters.x509().convert(input);
        }

        return rsaPublicKey;
    }


}
