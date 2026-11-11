package com.editorial.article.service;

import com.editorial.article.client.UserProfileClient;
import com.editorial.article.dto.*;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.exception.IncorrectArticleStatusException;
import com.editorial.article.model.Article;
import com.editorial.article.model.ArticleStatus;
import com.editorial.article.model.ModerationDecision;
import com.editorial.article.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private final ArticleRepository repository;
    private final UserProfileClient userProfileClient;
    public ArticleService(ArticleRepository repository, UserProfileClient userProfileClient) {
        this.userProfileClient = userProfileClient;
        this.repository = repository;
    }

    private void checkIfEditable(Article article, long accountId, String role) {
        if (role.equals("AUTHOR") && article.getStatus().name().equals("PENDING_REVIEW") && article.getAuthorAccountId() == accountId) throw new AccessDeniedException("Article is in review");
    }

    private void checkIfAccessible(Article article, long accountId, String role) {
        boolean isPrivileged = role.equals("ADMIN") || role.equals("EDITOR");
        boolean isOwnArticle = (article.getAuthorAccountId() == accountId) && (role.equals("AUTHOR"));

        if (!isPrivileged && !isOwnArticle) throw new AccessDeniedException("Not enough rights");
    }


    @Transactional(readOnly = true)
    public Page<GetArticlesResponse> getAll(Pageable request, ArticleStatus status, Long accountId) {
        Specification<Article> specification = Specification.allOf(ArticleSpecifications.hasAuthorAccountId(accountId).and(ArticleSpecifications.hasStatus(status)));

        Page<Article> articles =  repository.findAll(specification, request);

        Set<Long> authorIds = articles.getContent().stream().map(Article::getAuthorAccountId).collect(Collectors.toSet());

        Map<Long, String> displayNames = userProfileClient.getDisplayNames(authorIds);

        return articles.map(article ->
                new GetArticlesResponse(article.getId(),
                        article.getAuthorAccountId(),
                        article.getTitle(),
                        article.getContent(),
                        displayNames.get(article.getAuthorAccountId()),
                        article.getStatus(),
                        article.getCreatedAt(),
                        article.getPublishedAt()));
    }

    @Transactional(readOnly = true)
    public Page<Article> getMy(Pageable request, long accountId) {
        return repository.findAllByAuthorAccountId(accountId, request);
    }

    @Transactional(readOnly = true)
    public GetArticlesResponse findById(long id, String role, long accountId) {
        Article article = repository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);

        ArticleStatus status = article.getStatus();

        boolean isPublished = status == ArticleStatus.PUBLISHED;
        boolean isOwner = article.getAuthorAccountId() == accountId;

        if (role.equals("ADMIN")
                || isPublished
                || (role.equals("AUTHOR") && isOwner)
                || (role.equals("EDITOR") && status == ArticleStatus.PENDING_REVIEW)) {
           Map<Long, String> displayNames = userProfileClient.getDisplayNames(Set.of(article.getAuthorAccountId()));
           return new GetArticlesResponse(article.getId(),
                   article.getAuthorAccountId(),
                   article.getTitle(),
                   article.getContent(),
                   displayNames.get(article.getAuthorAccountId()),
                   article.getStatus(),
                   article.getCreatedAt(),
                   article.getPublishedAt());
        }

        throw new AccessDeniedException("Not enough rights to view this article");
        
    }

    @Transactional
    public Article add(CreateArticleRequest requestBody, Long accountId) {
        return repository.save(new Article(requestBody.title(), requestBody.content(), accountId, ArticleStatus.DRAFT));
    }

    @Transactional
    public Article updateById(UpdateArticleRequest request, long id, long accountId, String role){
        Article response = repository.findByIdForUpdate(id).orElseThrow(ArticleNotFoundException::new);
        if (response.getStatus().name().equals("PUBLISHED") || response.getStatus().name().equals("ARCHIVED")) throw new IncorrectArticleStatusException();
        checkIfEditable(response, accountId, role);
        checkIfAccessible(response, accountId, role);

        response.setTitle(request.title());
        response.setContent(request.content());
        response.setUpdatedAt(Instant.now());

        return response;
    }
    @Transactional
    public void delete(long id, long accountId, String role) {
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);
        checkIfEditable(response,accountId, role);
        checkIfAccessible(response, accountId, role);

        repository.deleteById(id);
    }

    @Transactional
    public Article patchById(PatchArticleRequest request, long id, long accountId, String role) {
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);
        String status = response.getStatus().name();

        if (status.equals("PUBLISHED") || status.equals("ARCHIVED")) throw new IncorrectArticleStatusException();
        checkIfEditable(response, accountId, role);
        checkIfAccessible(response, accountId, role);
        if (request.title() != null) response.setTitle(request.title());
        if (request.content() != null) response.setContent(request.content());

        response.setUpdatedAt(Instant.now());
        return response;
    }

    @Transactional
    public void submitForReview(long articleId, long accountId) {
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);

        String status = foundArticle.getStatus().name();

        if (foundArticle.getAuthorAccountId() != accountId) throw new AccessDeniedException("You can't perform this operation");
        if (!status.equals("DRAFT") && !status.equals("REJECTED") && !status.equals("ARCHIVED")) throw new IncorrectArticleStatusException();

        foundArticle.setStatus(ArticleStatus.valueOf("PENDING_REVIEW"));
        foundArticle.setRejectionReason("");
    }

    @Transactional(readOnly = true)
    public Page<Article> getPendingArticles(Pageable request) {
        return repository.findAllByStatus(ArticleStatus.PENDING_REVIEW, request);
    }

    @Transactional
    public void moderateArticle(long articleId, ModerationDecision decision, String rejectionReason) {
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);

        if (!foundArticle.getStatus().name().equals("PENDING_REVIEW")) throw new IncorrectArticleStatusException();

        switch (decision) {
            case APPROVED -> {
                foundArticle.setStatus(ArticleStatus.valueOf("PUBLISHED"));
                foundArticle.setPublishedAt(Instant.now());
                foundArticle.setRejectionReason("");
            }

            case REJECTED -> {
                foundArticle.setStatus(ArticleStatus.valueOf("REJECTED"));
                foundArticle.setRejectionReason(rejectionReason);
            }
        }
    }

    @Transactional
    public void archiveArticle(long articleId, long accountId, String role) {
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);

        if (!foundArticle.getStatus().name().equals("PUBLISHED")) throw new IncorrectArticleStatusException();
        checkIfAccessible(foundArticle, accountId, role);

        foundArticle.setStatus(ArticleStatus.ARCHIVED);

    }

    @Transactional
    public void withdrawReview(long articleId, long accountId, String role) {
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);
        checkIfEditable(foundArticle, accountId, role);

        foundArticle.setStatus(ArticleStatus.DRAFT);

    }
}
