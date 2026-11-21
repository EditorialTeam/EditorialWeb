package com.editorial.article;

import com.editorial.article.client.UserProfileClient;
import com.editorial.article.dto.article.CreateArticleRequest;
import com.editorial.article.model.entity.Article;
import com.editorial.article.repository.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ArticleApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("JWT_JWK_SET_URI", () -> "http://localhost:8080/.well-known/jwks.json");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @MockitoBean
    private UserProfileClient userProfileClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldCreateArticleAndPersistInDatabase() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest("Integration Article Title", "Integration Article Content");

        mockMvc.perform(post("/api/articles")
                        .with(jwt().subject("42").claim("role", "AUTHOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Article Title"))
                .andExpect(jsonPath("$.authorAccountId").value(42));

        assertFalse(articleRepository.findAll().isEmpty());
    }

    @Test
    void shouldGetArticleById() throws Exception {
        Article article = new Article("Saved Title", "Saved Content", 42L);
        Article saved = articleRepository.save(article);

        when(userProfileClient.getDisplayNames(any())).thenReturn(Map.of(42L, "Author 42"));

        mockMvc.perform(get("/api/articles/" + saved.getId())
                        .with(jwt().subject("42").claim("role", "AUTHOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Saved Title"));
    }
}
