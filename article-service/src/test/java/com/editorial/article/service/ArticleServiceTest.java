package com.editorial.article.service;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import com.editorial.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ArticleServiceTest {
    @Test
    public void shouldReturnArticleWhenExists() {
        ArticleRepository repository = mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repository);
        Article testArticle = new Article("", "");
        when(repository.findById(0L)).thenReturn(Optional.of(testArticle));

        Article result = service.findById(0L);

        assertSame(testArticle, result);
    }

    @Test
    public void shouldThrowWhenArticleNotFound() {
        ArticleRepository repository = mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repository);
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ArticleNotFoundException.class, () -> {
            service.findById(999L);
        });
    }

    @Test
    public void shouldCreateArticle() {
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        ArticleRepository repository = mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repository);
        CreateArticleRequest request = new CreateArticleRequest("Test", "Test");
        Article testArticle = new Article(request.title(), request.content());
        testArticle.setId(1L);
        when(repository.save(any(Article.class))).thenReturn(testArticle);

        Article result = service.add(request);

        assertSame(testArticle, result);

        verify(repository).save(captor.capture());

        Article captured = captor.getValue();
        assertEquals("Test", captured.getTitle());
        assertEquals("Test", captured.getContent());
    }

    @Test
    public void shouldUpdateArticle() {
        ArticleRepository repository = mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repository);

        UpdateArticleRequest request = new UpdateArticleRequest("New title", "New content");

        Article oldArticle = new Article("Old title", "Old content");
        oldArticle.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(oldArticle));

        Article result = service.updateById(request, 5L);

        assertEquals(oldArticle, result);
        assertEquals(request.title(), result.getTitle());
        assertEquals(request.content(), result.getContent());

        verify(repository, never()).save(any(Article.class));
    }
}
