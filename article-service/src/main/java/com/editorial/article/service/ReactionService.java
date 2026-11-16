package com.editorial.article.service;

import com.editorial.article.dto.reaction.CreateReactionResponse;
import com.editorial.article.dto.reaction.GetReactions;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.exception.ReactionNotFoundException;
import com.editorial.article.exception.UnknownReactionTypeException;
import com.editorial.article.model.entity.Article;
import com.editorial.article.model.entity.Reaction;
import com.editorial.article.model.enums.ReactionType;
import com.editorial.article.repository.ArticleRepository;
import com.editorial.article.repository.ReactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final ArticleRepository articleRepository;

    public ReactionService(ReactionRepository reactionRepository, ArticleRepository articleRepository) {
        this.reactionRepository = reactionRepository;
        this.articleRepository = articleRepository;
    }


    @Transactional
    public CreateReactionResponse putReaction(long authorAccountId, long articleId, String reactionType) {
        Article foundArticle = articleRepository.findById(articleId).orElseThrow(ArticleNotFoundException::new);

        ReactionType reaction;

        try {
            reaction = ReactionType.valueOf(reactionType);
        } catch (IllegalArgumentException e) {
            throw new UnknownReactionTypeException();
        }

        Optional<Reaction> foundReaction = reactionRepository.findByArticleIdAndAuthorAccountId(articleId, authorAccountId);

        if (foundReaction.isPresent()) {
            foundReaction.get().setReactionType(reaction);
        } else {
            reactionRepository.save(new Reaction(reaction, authorAccountId, foundArticle));
        }

        return new CreateReactionResponse(reaction);
    }

    @Transactional
    public void deleteReaction(long articleId, long authorAccountId) {
        if (!articleRepository.existsById(articleId)) throw new ArticleNotFoundException();

        reactionRepository.delete(
                reactionRepository
                .findByArticleIdAndAuthorAccountId(articleId, authorAccountId)
                .orElseThrow(ReactionNotFoundException::new)
        );
    }

    @Transactional
    public GetReactions getReactions(long articleId) {
        if (!articleRepository.existsById(articleId)) throw new ArticleNotFoundException();

        List<Object[]> reactionCount = reactionRepository.countReactionsByType(articleId);
        Map<ReactionType, Long> returnMap = new HashMap<>();

        for (Object[] count : reactionCount) {
            returnMap.put((ReactionType) count[0], (Long) count[1]);
        }

        return new GetReactions(returnMap);
    }
}
