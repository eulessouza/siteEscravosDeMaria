package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.Category;
import com.escravosdev.api.entities.enums.PostType;
import com.escravosdev.api.repo.CategoryRepo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepo categoryRepo;

    @Operation(summary = "Listar todas as categorias")
    @GetMapping
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(categoryRepo.findAll());
    }

    @Operation(summary = "Listar categorias por tipo")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> listByType(@PathVariable PostType type) {
        return ResponseEntity.ok(categoryRepo.findByType(type));
    }
}