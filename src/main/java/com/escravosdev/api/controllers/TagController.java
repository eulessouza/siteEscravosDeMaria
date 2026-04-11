package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.Tag;
import com.escravosdev.api.repo.TagRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepo tagRepo;

    
    @GetMapping
    public ResponseEntity<List<Tag>> list() {
        return ResponseEntity.ok(tagRepo.findAll());
    }
}