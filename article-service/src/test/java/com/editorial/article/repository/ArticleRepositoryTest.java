package com.editorial.article.repository;

import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;


import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //не хотим чтобы заменялась бд

@Testcontainers
public class ArticleRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6");

    @Autowired
    ArticleRepository repository;

    @Autowired
    EntityManager entityManager;

    @Test
    public void shouldSaveAndFindArticle() {
        Article article = new Article("Test", "Test");

        Article savedArticle = repository.save(article);

        long generatedId = savedArticle.getId();

        repository.flush();
        entityManager.clear();
        Article result = repository.findById(generatedId).orElseThrow(ArticleNotFoundException::new);

        assertNotSame(article, result);
        assertEquals(article.getContent(), result.getContent());
        assertEquals(article.getTitle(), result.getTitle());
    }

    @Test
    public void shouldUpdateWhenNotSaved() {
        Article testArticle = new Article("Test", "Test");

        Article savedArticle = repository.save(testArticle);

        repository.flush();
        entityManager.clear();

        Article result = repository.findById(savedArticle.getId()).orElseThrow(ArticleNotFoundException::new);

        result.setTitle("New test");

        entityManager.flush();
        entityManager.clear();

        Article reloaded = repository.findById(savedArticle.getId()).get();

        assertEquals("New test", reloaded.getTitle());
    }

    @Test
    public void shouldNotUpdateDetachedArticle() {
        Article original = new Article("Original", "Original");

        Article saved = repository.save(original);

        entityManager.flush();
        entityManager.clear();

        Article result = repository.findById(saved.getId()).orElseThrow(ArticleNotFoundException::new);

        entityManager.detach(result);
        result.setTitle("Changed");
        entityManager.flush();
        entityManager.clear();

        Article reloaded = repository.findById(saved.getId()).get();

        assertEquals("Original", reloaded.getTitle());
    }

    @Test
    public void shouldMergeDetachedArticle() {
        Article original = new Article("Original", "Original");

        long generatedId = repository.save(original).getId();

        Article result = repository.findById(generatedId).orElseThrow();

        entityManager.detach(result);
        result.setTitle("New");

        entityManager.flush();
        entityManager.clear();

        Article merged = entityManager.merge(result);

        assertTrue(entityManager.contains(merged));
        assertFalse(entityManager.contains(result));

        entityManager.flush();
        entityManager.clear();

        Article findMerged = repository.findById(generatedId).orElseThrow();

        assertEquals("New", findMerged.getTitle());
    }
}

