package com.escravosdev.api.dtos;

import java.util.List;

public record CreateQuestionRequest(
        String title,
        String content,
        List<String> tagSlugs,
        String categorySlug
) {}