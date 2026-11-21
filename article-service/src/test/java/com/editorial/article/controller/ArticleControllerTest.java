package com.editorial.article.controller;

import com.editorial.article.dto.article.CreateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.entity.Article;
import com.editorial.article.model.enums.ArticleStatus;
import com.editorial.article.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService service;

    private String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturn404WhenArticleNotFound() throws Exception {
        when(service.findById(999L, "ADMIN", 5L)).thenThrow(ArticleNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/articles/999")
                        .with(jwt().jwt(builder -> builder.subject("5").claim("role", "ADMIN"))))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ARTICLE_NOT_FOUND"));
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        CreateArticleRequest faultyArticle = new CreateArticleRequest("", "Content");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/articles")
                        .with(jwt().jwt(builder -> builder.subject("1").claim("role", "AUTHOR")))
                        .content(asJsonString(faultyArticle))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateArticle() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest("Content", "Content");
        Article saved = new Article(request.title(), request.content(), 1L, ArticleStatus.DRAFT);
        when(service.add(request, 1L)).thenReturn(saved);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/articles")
                        .with(jwt().jwt(builder -> builder.subject("1").claim("role", "AUTHOR")))
                        .content(asJsonString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(service).add(request, 1L);
    }
}
