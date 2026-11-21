package com.editorial.article.controller;

import com.editorial.article.dto.reaction.CreateReactionRequest;
import com.editorial.article.dto.reaction.CreateReactionResponse;
import com.editorial.article.dto.reaction.GetReactions;
import com.editorial.article.service.ReactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ReactionController {
    private final ReactionService service;

    public ReactionController(ReactionService service) {
        this.service = service;
    }

    @PutMapping("/{articleId}/reactions")
    public ResponseEntity<CreateReactionResponse> putReaction(@PathVariable long articleId,
                                                              @RequestBody CreateReactionRequest request,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.putReaction(Long.parseLong(jwt.getSubject()), articleId, request.reactionType()));
    }

    @DeleteMapping("/{articleId}/reactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReaction(@PathVariable long articleId,
                               @AuthenticationPrincipal Jwt jwt) {
        service.deleteReaction(articleId, Long.parseLong(jwt.getSubject()));
    }

    @GetMapping("/{articleId}/reactions")
    @ResponseStatus(HttpStatus.OK)
    public GetReactions getReactions(@PathVariable long articleId) {
        return service.getReactions(articleId);
    }
}
