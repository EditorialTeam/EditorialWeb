package com.editorial.article.model.entity;

import com.editorial.article.model.enums.ReactionType;
import jakarta.persistence.*;

@Entity
@Table(name="reactions")
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name="reaction_type", nullable = false)
    private ReactionType reactionType;

    @Column(name="author_account_id", nullable = false)
    private long authorAccountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="article_id", nullable = false)
    private Article parentArticle;

    protected Reaction() {}

    public long getAuthorAccountId() {
        return authorAccountId;
    }

    public Article getParentArticle() {
        return parentArticle;
    }

    public long getId() {
        return id;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
