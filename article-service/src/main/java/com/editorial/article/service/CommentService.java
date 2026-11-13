package com.editorial.article.service;

import com.editorial.article.client.UserProfileClient;
import com.editorial.article.dto.comment.GetCommentsResponse;
import com.editorial.article.dto.comment.PostCommentRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.exception.CommentNotFoundException;
import com.editorial.article.exception.IncorrectArticleStatusException;
import com.editorial.article.model.Article;
import com.editorial.article.model.ArticleStatus;
import com.editorial.article.model.Comment;
import com.editorial.article.repository.ArticleRepository;
import com.editorial.article.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserProfileClient client;


    public CommentService(CommentRepository commentRepository,
                          ArticleRepository articleRepository,
                          UserProfileClient client) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.client = client;
    }


    @Transactional
    public void postComment(PostCommentRequest request, long articleId, long authorAccountId) {
        Article foundArticle = articleRepository.findById(articleId).orElseThrow(ArticleNotFoundException::new);

        Long parentId = request.parentId();
        String content = request.content();

        if (foundArticle.getStatus() != ArticleStatus.PUBLISHED) throw new IncorrectArticleStatusException();

        Comment parentComment = null;

        if (parentId != null) {
            parentComment = commentRepository.findById(parentId).orElseThrow(CommentNotFoundException::new);
            if (parentComment.getArticle().getId() != articleId) throw new AccessDeniedException("Can't post comment here");
            if (parentComment.isDeleted()) throw new IllegalStateException();
        }

        commentRepository.save(new Comment(foundArticle,
                parentComment,
                authorAccountId,
                content));
    }

    @Transactional
    public Page<GetCommentsResponse> getRootComments(long articleId, Pageable request) {
        Page<Comment> response = commentRepository.findAllByArticleIdAndParentCommentIsNull(articleId, request);

        Map<Long, String> displayNames = client.getDisplayNames(response.getContent()
                .stream()
                .map(Comment::getAuthorAccountId).collect(Collectors.toSet()));

        return response.map(
                comment ->
                        new GetCommentsResponse(
                                comment.getId(),
                                articleId,
                                comment.getParentComment().getId(),
                                comment.getAuthorAccountId(),
                                displayNames.get(comment.getAuthorAccountId()),
                                comment.getContent(),
                                comment.getCreatedAt(),
                                comment.getUpdatedAt(),
                                comment.isDeleted()
                        )
        );
    }
}
