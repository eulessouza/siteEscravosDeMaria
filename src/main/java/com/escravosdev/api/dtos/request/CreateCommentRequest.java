package com.escravosdev.api.dtos.request;

import java.util.UUID;

public record CreateCommentRequest(
        String content,
        UUID parentId  // null = comentário raiz
) {}