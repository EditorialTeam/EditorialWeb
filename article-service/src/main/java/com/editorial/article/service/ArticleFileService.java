package com.editorial.article.service;

import com.editorial.article.client.RestFileServiceClient;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.entity.Article;
import com.editorial.article.model.entity.ArticleImage;
import com.editorial.article.repository.ArticleImageRepository;
import com.editorial.article.repository.ArticleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

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
}
