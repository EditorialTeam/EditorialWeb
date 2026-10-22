package com.editorial.article.controller;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import com.editorial.article.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
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
    void shouldReturnArticleById() throws Exception {
        Article original = new Article("Original", "Original");
        when(service.findById(5L)).thenReturn(original);

       mockMvc.perform(MockMvcRequestBuilders.get("/api/articles/5")).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Original"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("Original"));
    }

    @Test
    void shouldReturn404WhenArticleNotFound() throws Exception {
        when(service.findById(999L)).thenThrow(ArticleNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/articles/999")).andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ARTICLE_NOT_FOUND"));
    }

    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        Article faultyArticle = new Article("", "Content");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/articles").content(asJsonString(faultyArticle)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("Validation error"));
    }

    @Test
    void shouldCreateArticle() throws Exception {
        CreateArticleRequest request = new CreateArticleRequest("Content", "Content");
        Article requestBody = new Article(request.title(), request.content());
        when(service.add(request)).thenReturn(requestBody);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/articles").content(asJsonString(requestBody)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(service).add(request);
    }
}
