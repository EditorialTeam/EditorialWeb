package com.editorial.article.service;

import com.editorial.article.client.RestFileServiceClient;
import com.editorial.article.dto.article.GetArticleImagesResponse;
import com.editorial.article.exception.FileUploadException;
import com.editorial.article.model.entity.Article;
import com.editorial.article.model.entity.ArticleImage;
import com.editorial.article.model.enums.ArticleStatus;
import com.editorial.article.repository.ArticleImageRepository;
import com.editorial.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleFileServiceTest {
    @Mock
    ArticleRepository repository;
    @Mock
    ArticleImageRepository imageRepository;
    @Mock
    RestFileServiceClient restFileServiceClient;
    @InjectMocks
    ArticleFileService service;

    @Test
    public void shouldDeleteOldFileOnlyAfterCommit() {
        Article article = new Article("Title", "Content", 1L, ArticleStatus.DRAFT);
        article.setId(5L);
        ArticleImage image = new ArticleImage("old-key", article);
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(article));
        when(imageRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(image));
        when(restFileServiceClient.uploadFile(5L, "ARTICLE_IMAGE", file)).thenReturn("new-key");

        TransactionSynchronizationManager.initSynchronization();
        try {
            assertEquals("new-key", service.updateImage(1L, 5L, 10L, file, "AUTHOR"));
            assertEquals("new-key", image.getObjectKey());
            verify(restFileServiceClient, never()).deleteFile(anyString());

            TransactionSynchronizationManager.getSynchronizations().forEach(synchronization -> synchronization.afterCommit());
            verify(restFileServiceClient).deleteFile("old-key");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void shouldKeepOldKeyWhenUploadFails() {
        Article article = new Article("Title", "Content", 1L, ArticleStatus.DRAFT);
        article.setId(5L);
        ArticleImage image = new ArticleImage("old-key", article);
        MockMultipartFile file = new MockMultipartFile("file", new byte[]{1});
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(article));
        when(imageRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(image));
        when(restFileServiceClient.uploadFile(5L, "ARTICLE_IMAGE", file)).thenReturn(null);

        assertThrows(FileUploadException.class, () -> service.updateImage(1L, 5L, 10L, file, "AUTHOR"));
        assertEquals("old-key", image.getObjectKey());
        verify(restFileServiceClient, never()).deleteFile(anyString());
    }

    @Test
    public void shouldRejectImageFromAnotherArticle() {
        Article article = new Article("Title", "Content", 1L, ArticleStatus.DRAFT);
        article.setId(5L);
        Article anotherArticle = new Article("Title", "Content", 1L, ArticleStatus.DRAFT);
        anotherArticle.setId(6L);
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(article));
        when(imageRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(new ArticleImage("key", anotherArticle)));

        assertThrows(AccessDeniedException.class, () -> service.updateImage(1L, 5L, 10L, null, "AUTHOR"));
        verifyNoInteractions(restFileServiceClient);
    }

    @Test
    public void shouldGetPublishedArticleImages() {
        Article article = new Article("Title", "Content", 1L, ArticleStatus.PUBLISHED);
        ArticleImage image = new ArticleImage("key", article);
        when(repository.findById(5L)).thenReturn(Optional.of(article));
        when(imageRepository.findAllByArticleId(5L)).thenReturn(List.of(image));

        assertEquals(List.of(new GetArticleImagesResponse(image.getId(), "key")), service.getImages(5L, 2L, "USER"));
    }

    @Test
    public void shouldRejectReadingAnotherAuthorsDraft() {
        Article article = new Article("Title", "Content", 1L, ArticleStatus.DRAFT);
        when(repository.findById(5L)).thenReturn(Optional.of(article));

        assertThrows(AccessDeniedException.class, () -> service.getImages(5L, 2L, "AUTHOR"));
        verifyNoInteractions(imageRepository);
    }
}
