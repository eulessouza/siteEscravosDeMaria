package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.request.CreateCommentRequest;
import com.escravosdev.api.dtos.response.CommentResponse;
import com.escravosdev.api.entities.discord.DiscordRoles;
import com.escravosdev.api.services.CommentService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Listar comentários de um post")
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> listByPost(@PathVariable UUID postId) {
        Claims claims = null;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Claims) {
            claims = (Claims) auth.getPrincipal();
        }
        return ResponseEntity.ok(commentService.listByPost(postId, claims));
    }

    @Operation(summary = "Criar comentário")
    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID postId,
            @RequestBody CreateCommentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(postId, req, getClaims()));
    }

    @Operation(summary = "Deletar comentário — ADM")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommentResponse> delete(@PathVariable UUID commentId) {
        requireAdm();
        return ResponseEntity.ok(commentService.delete(commentId));
    }

    private void requireAdm() {
        if (!DiscordRoles.isAdm(getClaims())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Claims getClaims() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}