package com.editorial.article.service;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.PatchArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import com.editorial.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class ArticleServiceTest {
    @Mock
    ArticleRepository repository;

    @InjectMocks
    ArticleService service;

    @Test
    public void shouldReturnArticleWhenExists() {
        Article testArticle = new Article("", "", 1L);
        when(repository.findById(0L)).thenReturn(Optional.of(testArticle));

        Article result = service.findById(0L);

        assertSame(testArticle, result);
    }

    @Test
    public void shouldThrowWhenArticleNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ArticleNotFoundException.class, () -> {
            service.findById(999L);
        });
    }

    @Test
    public void shouldCreateArticle() {
        ArgumentCaptor<Article> captor = ArgumentCaptor.forClass(Article.class);
        CreateArticleRequest request = new CreateArticleRequest("Test", "Test");
        Article testArticle = new Article(request.title(), request.content(), 1L);
        testArticle.setId(1L);
        when(repository.save(any(Article.class))).thenReturn(testArticle);

        Article result = service.add(request, 1L);

        assertSame(testArticle, result);

        verify(repository).save(captor.capture());

        Article captured = captor.getValue();
        assertEquals("Test", captured.getTitle());
        assertEquals("Test", captured.getContent());
    }

    @Test
    public void shouldUpdateArticle() {

        UpdateArticleRequest request = new UpdateArticleRequest("New title", "New content");

        Article oldArticle = new Article("Old title", "Old content", 1L);
        oldArticle.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(oldArticle));

        Article result = service.updateById(request, 5L);

        assertEquals(oldArticle, result);
        assertEquals(request.title(), result.getTitle());
        assertEquals(request.content(), result.getContent());

        verify(repository, never()).save(any(Article.class));
    }

    @Test
    public void shouldUpdateOnlyTitle() {

        PatchArticleRequest request = new PatchArticleRequest("New title", null);

        Article oldArticle = new Article("Old title", "Old content", 1L);
        oldArticle.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(oldArticle));

        Article result = service.patchById(request, 5L);

        assertEquals(request.title(), result.getTitle());
        assertEquals("Old content", result.getContent());
        assertSame(oldArticle, result);

        verify(repository).findById(5L);
    }

    @Test
    public void shouldDelete() {
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        Article expected = new Article("Test", "Test", 1L);
        expected.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(expected));

        service.delete(5L);
        verify(repository).deleteById(captor.capture());

        assertSame(captor.getValue(), expected.getId());
    }

    @Test
    public void shouldThrowWhenNotFoundOnDelete() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ArticleNotFoundException.class, () -> service.delete(999L));

        verify(repository, never()).deleteById(any(Long.class));
    }
}
