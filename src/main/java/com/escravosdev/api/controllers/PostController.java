package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    public ResponseEntity<Map<String, Object>> createPost() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "ok"));
    }

}
