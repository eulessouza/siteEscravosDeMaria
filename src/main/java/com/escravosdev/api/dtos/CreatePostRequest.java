package com.escravosdev.api.dtos;

import com.escravosdev.api.entities.PostType;

public record CreatePostRequest(
        String title,
        String content,
        String coverImageUrl,
        PostType type
) {}
