package com.editorial.article.service;

import com.editorial.article.client.RestFileServiceClient;
import com.editorial.article.dto.article.GetArticleImagesResponse;
import com.editorial.article.exception.ArticleImageNotFoundException;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.exception.FileUploadException;
import com.editorial.article.exception.IncorrectArticleStatusException;
import com.editorial.article.exception.InvalidFileRequestException;
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

    private void checkIfEditable(Article article, long accountId, String role) {
        boolean isPrivileged = role.equals("ADMIN") || role.equals("EDITOR");
        boolean isOwnArticle = role.equals("AUTHOR") && article.getAuthorAccountId() == accountId;

        if (!isPrivileged && !isOwnArticle) throw new AccessDeniedException("Not enough rights");
        if (article.getStatus() == ArticleStatus.PUBLISHED || article.getStatus() == ArticleStatus.ARCHIVED) throw new IncorrectArticleStatusException();
        if (role.equals("AUTHOR") && article.getStatus() == ArticleStatus.PENDING_REVIEW) throw new AccessDeniedException("Article is in review");
    }

    private String uploadFile(long articleId, String category, MultipartFile file) {
        String key = restFileServiceClient.uploadFile(articleId, category, file);
        if (key == null || key.isBlank()) throw new FileUploadException("Couldn't upload file");
        return key;
    }

    @Transactional
    public String updateImage(long authorAccountId,
                              long articleId,
                              long imageId,
                              MultipartFile file,
                              String role) {
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);
        checkIfEditable(foundArticle, authorAccountId, role);
        ArticleImage foundImage = imageRepository.findByIdForUpdate(imageId).orElseThrow(ArticleImageNotFoundException::new);

        if (foundImage.getArticle().getId() != articleId) throw new AccessDeniedException("Image doesn't belong to this article");
        String key = uploadFile(articleId, "ARTICLE_IMAGE", file);

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
        if (!"COVER".equals(category) && !"ARTICLE_IMAGE".equals(category)) throw new InvalidFileRequestException("Unknown file category");
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);
        checkIfEditable(foundArticle, authorAccountId, role);

        if (category.equals("COVER")) {
            String key = uploadFile(articleId, category, file);
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
            String key = uploadFile(articleId, category, file);
            imageRepository.save(new ArticleImage(key, foundArticle));

            return key;
        }

        throw new InvalidFileRequestException("Unknown file category");
    }

    @Transactional
    public void delete(long authorAccountId, long articleId, long imageId, String role) {
        Article foundArticle = repository.findByIdForUpdate(articleId).orElseThrow(ArticleNotFoundException::new);
        checkIfEditable(foundArticle, authorAccountId, role);
        ArticleImage foundImage = imageRepository.findById(imageId).orElseThrow(ArticleImageNotFoundException::new);

        if (foundImage.getArticle().getId() != articleId) throw new AccessDeniedException("Image doesn't belong to this article");

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
