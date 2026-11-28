package com.editorial.article;

import com.editorial.article.client.UserProfileClient;
import com.editorial.article.dto.article.CreateArticleRequest;
import com.editorial.article.dto.client.UserProfileInfoDto;
import com.editorial.article.model.entity.Article;
import com.editorial.article.model.enums.ArticleStatus;
import com.editorial.article.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
    }

    @Test
    void shouldCreateArticleAndPersistInDatabase() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest("Integration Article Title", "Integration Article Content");

        mockMvc.perform(post("/api/articles")
                        .with(jwt().jwt(builder -> builder.subject("42").claim("role", "AUTHOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_AUTHOR")))
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
        Article article = new Article("Saved Title", "Saved Content", 42L, ArticleStatus.DRAFT);
        Article saved = articleRepository.save(article);

        when(userProfileClient.getUserProfileInfo(any())).thenReturn(Map.of(42L, new UserProfileInfoDto("Author 42", null)));

        mockMvc.perform(get("/api/articles/" + saved.getId())
                        .with(jwt().jwt(builder -> builder.subject("42").claim("role", "AUTHOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_AUTHOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Saved Title"))
                .andExpect(jsonPath("$.authorInfo['42'].displayName").value("Author 42"));
    }

    @Test
    void shouldReturnOnlyPublishedArticlesForRegularUser() throws Exception {
        Article published = articleRepository.save(new Article("Published Title", "Published Content", 10L, ArticleStatus.PUBLISHED));
        articleRepository.save(new Article("Draft Title", "Secret Draft Content", 10L, ArticleStatus.DRAFT));

        Article rejected = new Article("Rejected Title", "Secret Rejected Content", 10L, ArticleStatus.REJECTED);
        rejected.setRejectionReason("Private moderator reason");
        articleRepository.save(rejected);

        when(userProfileClient.getUserProfileInfo(any())).thenReturn(Map.of(10L, new UserProfileInfoDto("Author 10", null)));

        var userJwt = jwt().jwt(builder -> builder.subject("99").claim("role", "USER"))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));

        mockMvc.perform(get("/api/articles").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].articleId").value(published.getId()))
                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"));

        mockMvc.perform(get("/api/articles").param("status", "DRAFT").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].articleId").value(published.getId()))
                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"));

        mockMvc.perform(get("/api/articles").param("status", "REJECTED").with(userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].articleId").value(published.getId()))
                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"));
    }

    @Test
    void shouldAllowFilteringByStatusForAdminAndEditor() throws Exception {
        articleRepository.save(new Article("Published Title", "Published Content", 10L, ArticleStatus.PUBLISHED));
        Article draft = articleRepository.save(new Article("Draft Title", "Draft Content", 10L, ArticleStatus.DRAFT));

        Article rejected = new Article("Rejected Title", "Rejected Content", 10L, ArticleStatus.REJECTED);
        rejected.setRejectionReason("Needs sources");
        articleRepository.save(rejected);

        when(userProfileClient.getUserProfileInfo(any())).thenReturn(Map.of(10L, new UserProfileInfoDto("Author 10", null)));

        mockMvc.perform(get("/api/articles")
                        .param("status", "DRAFT")
                        .with(jwt().jwt(builder -> builder.subject("1").claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].articleId").value(draft.getId()))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"));

        mockMvc.perform(get("/api/articles")
                        .param("status", "REJECTED")
                        .with(jwt().jwt(builder -> builder.subject("2").claim("role", "EDITOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_EDITOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].articleId").value(rejected.getId()))
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.content[0].rejectionReason").value("Needs sources"));

        mockMvc.perform(get("/api/articles")
                        .with(jwt().jwt(builder -> builder.subject("1").claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }
}
