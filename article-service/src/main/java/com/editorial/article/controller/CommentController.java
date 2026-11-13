package com.editorial.article.controller;

import com.editorial.article.dto.comment.GetCommentsResponse;
import com.editorial.article.dto.comment.PostCommentRequest;
import com.editorial.article.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class CommentController {
    private final CommentService service;

    public CommentController(CommentService service) {
        this.service = service;
    }

    @PostMapping("/{articleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public void postComment(@PathVariable long articleId,
                            @Valid @RequestBody PostCommentRequest request,
                            @AuthenticationPrincipal Jwt jwt) {
        service.postComment(request, articleId, Long.parseLong(jwt.getSubject()));
    }

    @GetMapping("/{articleId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public Page<GetCommentsResponse> getRootComments(@RequestParam(defaultValue = "0", name="page") int page,
                                                     @RequestParam(defaultValue = "20", name="size") int size,
                                                     @PathVariable long articleId) {
        return service.getRootComments(articleId, PageRequest.of(page, size, Sort.by("created_at").descending().and(Sort.by("id").ascending())));
    }
}
