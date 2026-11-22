package com.editorial.article.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="article_images")
public class ArticleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="object_key", columnDefinition = "text")
    private String objectKey;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="article_id", nullable = false)
    private Article article;

    protected ArticleImage() {}

    public ArticleImage(String objectKey, Article article) {
        this.objectKey = objectKey;
        this.article = article;
    }
    public long getId() {
        return id;
    }

    public Article getArticle() {
        return article;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
