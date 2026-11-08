package com.editorial.auth.service;

import com.editorial.auth.dto.UpdateRefreshTokenTokenServiceResponse;
import com.editorial.auth.exception.InvalidRefreshTokenException;
import com.editorial.auth.model.Account;
import com.editorial.auth.model.RefreshToken;
import com.editorial.auth.repository.AccountRepository;
import com.editorial.auth.repository.RefreshTokenRepository;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository tokenRepository;
    public RefreshTokenService(RefreshTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateRandomString() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    @Transactional
    public String generateRefreshToken(Account foundAccount) {

        String randomString = generateRandomString();
        String hashedRandomString = org.apache.commons.codec.digest.DigestUtils.sha256Hex(randomString);
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(30 * 24 * 60 * 60);

        RefreshToken refreshToken = new RefreshToken(foundAccount, hashedRandomString, createdAt, expiresAt, null);

        tokenRepository.save(refreshToken);

        return randomString;
    }

    @Transactional

    public UpdateRefreshTokenTokenServiceResponse updateRefreshToken(String refreshTokenOld) {
        String oldHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(refreshTokenOld);

        RefreshToken oldToken = tokenRepository.findByTokenHash(oldHash).orElseThrow(InvalidRefreshTokenException::new);

        if ((oldToken.getExpiresAt().isAfter(Instant.now())) && oldToken.getRevokedAt() == null) {
            Account accountByToken = oldToken.getLinkedAccount();

            tokenRepository.delete(oldToken);

            return new UpdateRefreshTokenTokenServiceResponse(generateRefreshToken(accountByToken), accountByToken);
        }

        else {
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
    }

    @Transactional
    public void removeRefreshToken(String oldRefreshToken) {
        String oldHash = org.apache.commons.codec.digest.DigestUtils.sha256Hex(oldRefreshToken);

        RefreshToken oldToken = tokenRepository.findByTokenHash(oldHash).orElseThrow(InvalidRefreshTokenException::new);

        tokenRepository.delete(oldToken);
    }
}
