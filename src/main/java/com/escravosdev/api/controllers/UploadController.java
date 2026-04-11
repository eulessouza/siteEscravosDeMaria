package com.escravosdev.api.controllers;

import com.escravosdev.api.services.UploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    
    @PostMapping("/blog")
    public ResponseEntity<Map<String, String>> uploadBlog(
            @RequestParam("file") MultipartFile file
    ) {
        var url = uploadService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    
    @PostMapping("/forum")
    public ResponseEntity<Map<String, String>> uploadForum(
            @RequestParam("file") MultipartFile file
    ) {
        var url = uploadService.uploadForumFile(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    
    @PostMapping("/forum/batch")
    public ResponseEntity<Map<String, List<String>>> uploadForumBatch(
            @RequestParam("files") List<MultipartFile> files
    ) {
        if (files.size() > 10) {
            return ResponseEntity.badRequest().build();
        }
        var urls = files.stream().map(uploadService::uploadForumFile).toList();
        return ResponseEntity.ok(Map.of("urls", urls));
    }
}