package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.Category;
import com.escravosdev.api.entities.enums.PostType;
import com.escravosdev.api.repo.CategoryRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepo categoryRepo;

    
    @GetMapping
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(categoryRepo.findAll());
    }

    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Category>> listByType(@PathVariable PostType type) {
        return ResponseEntity.ok(categoryRepo.findByType(type));
    }
}