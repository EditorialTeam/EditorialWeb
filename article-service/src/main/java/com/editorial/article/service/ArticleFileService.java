package com.editorial.article.service;

import com.editorial.article.client.RestFileServiceClient;
import com.editorial.article.dto.article.GetArticleImagesResponse;
import com.editorial.article.exception.ArticleImageNotFoundException;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.entity.Article;
import com.editorial.article.model.entity.ArticleImage;
import com.editorial.article.model.enums.ArticleStatus;
import com.editorial.article.repository.ArticleImageRepository;
import com.editorial.article.repository.ArticleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ArticleFileService {
    private final RestFileServiceClient restFileServiceClient;
    private final ArticleRepository repository;
    private final ArticleImageRepository imageRepository;

    public ArticleFileService(RestFileServiceClient restFileServiceClient,
                              ArticleRepository repository,
                              ArticleImageRepository imageRepository) {
        this.restFileServiceClient = restFileServiceClient;
        this.repository = repository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public String updateImage(long authorAccountId,
                              long articleId,
                              long imageId,
                              MultipartFile file,
                              String role) {
        Article foundArticle = repository.findById(articleId).orElseThrow(ArticleNotFoundException::new);
        ArticleImage foundImage = imageRepository.findById(imageId).orElseThrow(ArticleImageNotFoundException::new);

        if (foundImage.getArticle().getId() != articleId) throw new AccessDeniedException("Image doesn't belong to this article");
        if ((foundArticle.getAuthorAccountId() != authorAccountId) && !role.equals("ADMIN") && !role.equals("EDITOR")) throw new AccessDeniedException("Not enough rights");

        String key = restFileServiceClient.uploadFile(articleId, "ARTICLE_IMAGE", file);
        if (key == null || key.isBlank()) throw new IllegalStateException("Couldn't upload file");

        String oldKey = foundImage.getObjectKey();
        foundImage.setObjectKey(key);

        if (oldKey != null) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            restFileServiceClient.deleteFile(oldKey);
                        }
                    }
            );
        }

        return key;
    }

    @Transactional(readOnly = true)
    public List<GetArticleImagesResponse> getImages(long articleId, long accountId, String role) {
        Article foundArticle = repository.findById(articleId).orElseThrow(ArticleNotFoundException::new);
        ArticleStatus status = foundArticle.getStatus();

        boolean isPublished = status == ArticleStatus.PUBLISHED;
        boolean isOwner = foundArticle.getAuthorAccountId() == accountId;

        if (role.equals("ADMIN")
                || isPublished
                || (role.equals("AUTHOR") && isOwner)
                || (role.equals("EDITOR") && status == ArticleStatus.PENDING_REVIEW)) {
            return imageRepository.findAllByArticleId(articleId).stream()
                    .map(image -> new GetArticleImagesResponse(image.getId(), image.getObjectKey()))
                    .toList();
        }

        throw new AccessDeniedException("Not enough rights to view this article");
    }

    @Transactional
    public String upload(long authorAccountId,
                         long articleId,
                         MultipartFile file,
                         String category,
                         String role) {
        Article foundArticle = repository.findById(articleId).orElseThrow(ArticleNotFoundException::new);

        if ((foundArticle.getAuthorAccountId() != authorAccountId) && !role.equals("ADMIN") && !role.equals("EDITOR")) throw new AccessDeniedException("Not enough rights");

        if (category.equals("COVER")) {
            String key = restFileServiceClient.uploadFile(articleId, category, file);
            String oldKey = foundArticle.getCoverObjectKey();

            foundArticle.setCoverObjectKey(key);

            if (oldKey != null) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                restFileServiceClient.deleteFile(oldKey);
                            }
                        }
                );
            }
            return key;
        } else if (category.equals("ARTICLE_IMAGE")) {
            String key = restFileServiceClient.uploadFile(articleId, category, file);
            imageRepository.save(new ArticleImage(key, foundArticle));

            return key;
        }

        throw new IllegalStateException("");
    }

    @Transactional
    public void delete(long authorAccountId, long articleId, long imageId, String role) {
        Article foundArticle = repository.findById(articleId).orElseThrow(ArticleNotFoundException::new);
        ArticleImage foundImage = imageRepository.findById(imageId).orElseThrow(ArticleImageNotFoundException::new);

        if (foundImage.getArticle().getId() != articleId) throw new AccessDeniedException("Image doesn't belong to this article");
        if ((foundArticle.getAuthorAccountId() != authorAccountId) && !role.equals("ADMIN") && !role.equals("EDITOR")) throw new AccessDeniedException("Not enough rights");

        String objectKey = foundImage.getObjectKey();
        imageRepository.delete(foundImage);

        if (objectKey != null) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            restFileServiceClient.deleteFile(objectKey);
                        }
                    }
            );
        }
    }
}
