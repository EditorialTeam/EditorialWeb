package com.editorial.user.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="account_id", nullable = false, unique = true)
    private Long account_id;

    @Column(name="display_name", nullable = false, length=100)
    private String displayName;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @PrePersist
    protected void onCreated() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    protected User() {}
}
