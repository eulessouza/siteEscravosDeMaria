package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.request.CreateBlogPostRequest;
import com.escravosdev.api.dtos.request.UpdateBlogPostRequest;
import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.entities.discord.DiscordRoles;
import com.escravosdev.api.services.BlogService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "Listar todos os posts do blog")
    @GetMapping
    public ResponseEntity<List<PostResponse>> list() {
        return ResponseEntity.ok(blogService.list());
    }

    @Operation(summary = "Buscar post por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(blogService.getById(id, getOptionalClaims()));
    }

    @Operation(summary = "Criar post no blog — só ADM")
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody CreateBlogPostRequest req) {
        var claims = getClaims();
        if (!DiscordRoles.canCreateBlogPost(claims)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para postar no blog");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.create(req, claims));
    }

    @Operation(summary = "Editar post do blog — autor")
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateBlogPostRequest req
    ) {
        return ResponseEntity.ok(blogService.update(id, req, getClaims()));
    }

    @Operation(summary = "Deletar post do blog — ADM")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        var claims = getClaims();
        if (!DiscordRoles.isAdm(claims)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para deletar");
        }
        blogService.delete(id, claims);
        return ResponseEntity.noContent().build();
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
