package com.editorial.article.service;

import com.editorial.article.client.UserProfileClient;
import com.editorial.article.dto.article.CreateArticleRequest;
import com.editorial.article.dto.article.GetArticlesResponse;
import com.editorial.article.dto.article.PatchArticleRequest;
import com.editorial.article.dto.article.UpdateArticleRequest;
import com.editorial.article.dto.client.UserProfileInfoDto;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.enums.ArticleStatus;
import com.editorial.article.model.entity.Article;
import com.editorial.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class ArticleServiceTest {
    @Mock
    ArticleRepository repository;

    @Mock
    UserProfileClient userProfileClient;

    @InjectMocks
    ArticleService service;


    @Test
    public void shouldThrowWhenArticleNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ArticleNotFoundException.class, () -> {
            service.findById(999L, "ADMIN", 5L);
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

        Article oldArticle = new Article("Old title", "Old content", 1L, ArticleStatus.DRAFT);
        oldArticle.setId(5L);

        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(oldArticle));

        Article result = service.updateById(request, 5L, 1L, "ADMIN");

        assertEquals(oldArticle, result);
        assertEquals(request.title(), result.getTitle());
        assertEquals(request.content(), result.getContent());

        verify(repository, never()).save(any(Article.class));
    }

    @Test
    public void shouldUpdateOnlyTitle() {

        PatchArticleRequest request = new PatchArticleRequest("New title", null);

        Article oldArticle = new Article("Old title", "Old content", 1L, ArticleStatus.DRAFT);
        oldArticle.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(oldArticle));

        Article result = service.patchById(request, 5L, 1L, "ADMIN");

        assertEquals(request.title(), result.getTitle());
        assertEquals("Old content", result.getContent());
        assertSame(oldArticle, result);

        verify(repository).findById(5L);
    }

    @Test
    public void shouldDelete() {
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        Article expected = new Article("Test", "Test", 1L, ArticleStatus.DRAFT);
        expected.setId(5L);

        when(repository.findById(5L)).thenReturn(Optional.of(expected));

        service.delete(5L, 1L, "ADMIN");
        verify(repository).deleteById(captor.capture());

        assertSame(captor.getValue(), expected.getId());
    }

    @Test
    public void shouldThrowWhenNotFoundOnDelete() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ArticleNotFoundException.class, () -> service.delete(999L, 1L, "ADMIN"));

        verify(repository, never()).deleteById(any(Long.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldForcePublishedStatusWhenGetAllCalledByRegularUser() {
        Pageable pageable = PageRequest.of(0, 20);
        Article published = new Article("Published", "Content", 10L, ArticleStatus.PUBLISHED);
        published.setId(1L);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(published)));
        when(userProfileClient.getUserProfileInfo(Set.of(10L)))
                .thenReturn(Map.of(10L, new UserProfileInfoDto("Author 10", null)));

        Page<GetArticlesResponse> result = service.getAll(pageable, ArticleStatus.DRAFT, null, "USER");

        assertEquals(1, result.getContent().size());
        assertEquals(ArticleStatus.PUBLISHED, result.getContent().get(0).status());

        ArgumentCaptor<Specification<Article>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        var root = mock(jakarta.persistence.criteria.Root.class);
        var query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        var cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        var statusPath = mock(jakarta.persistence.criteria.Path.class);
        when(root.get("status")).thenReturn(statusPath);

        specCaptor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(statusPath, ArticleStatus.PUBLISHED);
        verify(cb, never()).equal(statusPath, ArticleStatus.DRAFT);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAllowDraftStatusWhenGetAllCalledByAdmin() {
        Pageable pageable = PageRequest.of(0, 20);
        Article draft = new Article("Draft", "Content", 10L, ArticleStatus.DRAFT);
        draft.setId(2L);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(draft)));
        when(userProfileClient.getUserProfileInfo(Set.of(10L)))
                .thenReturn(Map.of(10L, new UserProfileInfoDto("Author 10", null)));

        Page<GetArticlesResponse> result = service.getAll(pageable, ArticleStatus.DRAFT, null, "ADMIN");

        assertEquals(1, result.getContent().size());
        assertEquals(ArticleStatus.DRAFT, result.getContent().get(0).status());

        ArgumentCaptor<Specification<Article>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        var root = mock(jakarta.persistence.criteria.Root.class);
        var query = mock(jakarta.persistence.criteria.CriteriaQuery.class);
        var cb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
        var statusPath = mock(jakarta.persistence.criteria.Path.class);
        when(root.get("status")).thenReturn(statusPath);

        specCaptor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(statusPath, ArticleStatus.DRAFT);
    }
}
