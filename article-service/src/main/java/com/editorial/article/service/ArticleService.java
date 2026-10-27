package com.editorial.article.service;

import com.editorial.article.dto.CreateArticleRequest;
import com.editorial.article.dto.PatchArticleRequest;
import com.editorial.article.dto.UpdateArticleRequest;
import com.editorial.article.exception.ArticleNotFoundException;
import com.editorial.article.model.Article;
import com.editorial.article.repository.ArticleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {
    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    private void checkIfAccessible(Article article, long accountId, String role) {
        boolean isPrivileged = role.equals("ADMIN") || role.equals("EDITOR");
        boolean isOwnArticle = (article.getAuthorAccountId() == accountId) && (role.equals("AUTHOR"));

        if (!isPrivileged && !isOwnArticle) throw new AccessDeniedException("Not enough rights");
    }


    @Transactional(readOnly = true)
    public List<Article> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Article findById(long id) {
        Optional<Article> response = repository.findById(id);
        return response.orElseThrow(ArticleNotFoundException::new);
    }

    @Transactional
    public Article add(CreateArticleRequest requestBody, Long accountId) {
        return repository.save(new Article(requestBody.title(), requestBody.content(), accountId));
    }

    @Transactional
    public Article updateById(UpdateArticleRequest request, long id, long accountId, String role){
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);

        checkIfAccessible(response, accountId, role);

        response.setTitle(request.title());
        response.setContent(request.content());

        return response;
    }
    @Transactional
    public void delete(long id, long accountId, String role) {
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);

        checkIfAccessible(response, accountId, role);

        repository.deleteById(id);
    }

    @Transactional
    public Article patchById(PatchArticleRequest request, long id, long accountId, String role) {
        Article response = repository.findById(id).orElseThrow(ArticleNotFoundException::new);

        checkIfAccessible(response, accountId, role);

        if (request.title() != null) response.setTitle(request.title());
        if (request.content() != null) response.setContent(request.content());
        return response;
    }
}
