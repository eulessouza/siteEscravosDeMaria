package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.entities.discord.DiscordRoles;
import com.escravosdev.api.entities.enums.PostStatus;
import com.escravosdev.api.repo.PostRepo;
import com.escravosdev.api.services.ModerationService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;
    private final PostRepo postRepo;

    @Operation(summary = "Listar posts pendentes de aprovação — ADM")
    @GetMapping("/pending")
    public ResponseEntity<List<PostResponse>> listPending() {
        requireAdm();
        return ResponseEntity.ok(moderationService.listPending());
    }

    @Operation(summary = "Aprovar post — ADM (exceto Conde)")
    @PostMapping("/approve/{id}")
    public ResponseEntity<PostResponse> approve(@PathVariable UUID id) {
        requireAdmCanApprove();
        log.info("Post aprovado: {}", id);
        return ResponseEntity.ok(moderationService.approve(id, getClaims()));
    }

    @Operation(summary = "Rejeitar post — ADM")
    @PostMapping("/reject/{id}")
    public ResponseEntity<PostResponse> reject(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason
    ) {
        requireAdm();
        return ResponseEntity.ok(moderationService.reject(id, getClaims()));
    }

    @Operation(summary = "Listar rejeitados — ADM")
    @GetMapping("/rejected")
    public ResponseEntity<List<PostResponse>> listRejected() {
        requireAdm();
        return ResponseEntity.ok(moderationService.listRejected());
    }

    @Operation(summary = "Arquivar post — ADM")
    @PostMapping("/archive/{id}")
    public ResponseEntity<PostResponse> archive(@PathVariable UUID id) {
        requireAdm();
        return ResponseEntity.ok(moderationService.archive(id, getClaims()));
    }

    @Operation(summary = "Listar arquivados — ADM")
    @GetMapping("/archived")
    public ResponseEntity<List<PostResponse>> listArchived() {
        requireAdm();
        return ResponseEntity.ok(moderationService.listArchived());
    }

    @Operation(summary = "Fechar dúvida — Orientador ou ADM")
    @PostMapping("/close/{id}")
    public ResponseEntity<PostResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(moderationService.close(id, getClaims()));
    }

    @Operation(summary = "Listar fechados — ADM")
    @GetMapping("/closed")
    public ResponseEntity<List<PostResponse>> listClosed() {
        requireAdm();
        return ResponseEntity.ok(moderationService.listClosed());
    }

    @Operation(summary = "Reabrir dúvida — autor, Orientador ou ADM")
    @PostMapping("/reopen/{id}")
    public ResponseEntity<PostResponse> reopen(@PathVariable UUID id) {
        return ResponseEntity.ok(moderationService.reopen(id, getClaims()));
    }

    @Operation(summary = "Desarquivar post — blog: ADM | fórum/dúvidas: autor ou ADM")
    @PostMapping("/unarchive/{id}")
    public ResponseEntity<PostResponse> unarchive(@PathVariable UUID id) {
        return ResponseEntity.ok(moderationService.unarchive(id, getClaims()));
    }

    private void requireAdm() {
        if (!DiscordRoles.isAdm(getClaims())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão");
        }
    }

    private void requireAdmCanApprove() {
        var claims = getClaims();
        var canApprove = DiscordRoles.hasRole(claims, DiscordRoles.REI) ||
                DiscordRoles.hasRole(claims, DiscordRoles.AUTORIDADE_REAL) ||
                DiscordRoles.hasRole(claims, DiscordRoles.DUQUE) ||
                DiscordRoles.hasRole(claims, DiscordRoles.MARQUES) ||
                DiscordRoles.hasRole(claims, DiscordRoles.ROLE_SECRETA);
        if (!canApprove) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para aprovar");
        }
    }

    private Claims getClaims() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}