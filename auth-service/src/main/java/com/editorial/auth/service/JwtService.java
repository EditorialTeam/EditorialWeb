package com.editorial.auth.service;

import com.editorial.auth.model.Account;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey.Builder;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final RSAPublicKey publicKey;
    public JwtService(JwtEncoder jwtEncoder, RSAPublicKey publicKey) {
        this.jwtEncoder = jwtEncoder;
        this.publicKey = publicKey;
    }

    public String generateAccessToken(Account account) {
        Instant issuedAt = Instant.now();

         JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("editorial-auth-service")
                .subject(account.getId().toString())
                .audience(List.of("editorial-api"))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .claim("role", account.getRole().name())
                 .build();


        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId("editorial-key-1").build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Map<String, Object> getPublicKeys() {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)

                .keyID("editorial-key-1")
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return jwkSet.toJSONObject();
    }
}
