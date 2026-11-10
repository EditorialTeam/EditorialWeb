package com.editorial.article.model;

import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="author_account_id", nullable=false)
    private long authorAccountId;

    @Column(name="created_at", nullable = false)
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @Column(name="published_at")
    private Instant publishedAt;

    @Column(name="rejection_reason", columnDefinition = "text")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status;

    //для jpa
    protected Article() {
    }

    public Article(String title, String content, Long authorAccountId) {
        this.title = title;
        this.content = content;
        this.authorAccountId = authorAccountId;
    }


    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getAuthorAccountId() {return authorAccountId;}

    public ArticleStatus getStatus() {
        return status;
    }

    //testing
    public void setId(Long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
