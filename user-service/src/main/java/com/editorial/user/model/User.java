package com.editorial.user.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="account_id", nullable = false, unique = true)
    private Long accountId;

    @Column(nullable = false, columnDefinition = "text")
    private String username;

    @Column(name = "display_name", columnDefinition = "text")
    private String displayName;

    @Column(name="profile_description", columnDefinition = "text")
    private String profileDescription;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;




    @PrePersist
    protected void onCreated() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    protected User() {}

    public User(Long accountId, String username) {
        this.accountId = accountId;
        this.username = username;
    }

    public User(String displayName, String profileDescription) {
        this.displayName = displayName;
        this.profileDescription = profileDescription;
    }

    public long getAccountId() {
        return accountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getProfileId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
