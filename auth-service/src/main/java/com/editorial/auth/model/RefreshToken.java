package com.editorial.auth.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="account_id", nullable = false)
    private Account account;

    @Column(name="token_hash", nullable = false, unique = true, length=64)
    private String tokenHash;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name="revoked_at")
    private Instant revokedAt;

    protected RefreshToken() {}

    public RefreshToken(Account account, String tokenHash, Instant createdAt, Instant expiresAt, Instant revokedAt) {
        this.account = account;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public long getId() {
        return id;
    }

    public Account getLinkedAccount() {
        return account;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getTokenHash() {
        return tokenHash;
    }
}
