package com.editorial.article.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Comment parentComment;

    @Column(name="author_account_id", nullable = false)
    private long authorAccountId;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @Column(name="is_deleted", nullable = false)
    private boolean deleted;

    protected Comment() {}

    public Comment(Article article,
                   Comment parentComment,
                   long authorAccountId,
                   String content) {
        this.article = article;
        this.parentComment = parentComment;
        this.authorAccountId = authorAccountId;
        this.content = content;
    }

    @PrePersist
    protected void onCreated() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public long getId() {
        return id;
    }

    public long getAuthorAccountId() {
        return authorAccountId;
    }

    public Article getArticle() {
        return article;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return createdAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getContent() {
        return content;
    }
}
