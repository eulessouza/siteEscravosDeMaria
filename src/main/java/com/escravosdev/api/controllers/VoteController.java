package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.response.VoteResponse;
import com.escravosdev.api.entities.enums.VoteType;
import com.escravosdev.api.services.VoteService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @Operation(summary = "Votar em post do fórum")
    @PostMapping("/post/{postId}")
    public ResponseEntity<VoteResponse> voteOnPost(
            @PathVariable UUID postId,
            @RequestParam VoteType type
    ) {
        return ResponseEntity.ok(voteService.voteOnPost(postId, type, getClaims()));
    }

    @Operation(summary = "Votar em comentário")
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<VoteResponse> voteOnComment(
            @PathVariable UUID commentId,
            @RequestParam VoteType type
    ) {
        return ResponseEntity.ok(voteService.voteOnComment(commentId, type, getClaims()));
    }

    private Claims getClaims() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}