package com.editorial.article.controller;

import com.editorial.article.dto.comment.PostCommentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class CommentController {

    @PostMapping("/{articleId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public void postComment(@PathVariable long articleId,
                            @Valid @RequestBody PostCommentRequest request,
                            @AuthenticationPrincipal Jwt jwt) {

    }
}
