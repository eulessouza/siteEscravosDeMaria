package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.request.CreateForumPostRequest;
import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.services.ForumService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;

    @Operation(summary = "Listar posts do fórum")
    @GetMapping
    public ResponseEntity<List<PostResponse>> list() {
        return ResponseEntity.ok(forumService.list(getOptionalClaims()));
    }

    @Operation(summary = "Buscar post por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(forumService.getById(id, getOptionalClaims()));
    }

    @Operation(summary = "Criar post no fórum")
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody CreateForumPostRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(forumService.create(req, getClaims()));
    }

    private Claims getClaims() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Claims getOptionalClaims() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Claims c) return c;
        return null;
    }
}
