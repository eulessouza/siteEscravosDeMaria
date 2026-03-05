package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.request.CreateQuestionRequest;
import com.escravosdev.api.dtos.response.PostResponse;
import com.escravosdev.api.services.QuestionService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "Listar dúvidas")
    @GetMapping
    public ResponseEntity<List<PostResponse>> list() {
        return ResponseEntity.ok(questionService.list());
    }

    @Operation(summary = "Buscar dúvida por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @Operation(summary = "Criar dúvida")
    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody CreateQuestionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.create(req, getClaims()));
    }

    private Claims getClaims() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}