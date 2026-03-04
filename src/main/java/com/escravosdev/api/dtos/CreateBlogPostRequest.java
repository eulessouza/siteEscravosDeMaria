package com.escravosdev.api.dtos;

import java.util.List;

public record CreateBlogPostRequest(
        String title,
        String content,
        String coverImageUrl,
        List<String> imageUrls,     // imagens do corpo do post em ordem
        List<String> tagSlugs,      // slugs das tags
        String categorySlug,
        boolean publishToSite,
        boolean publishToDiscord,
        boolean publishToInstagram
) {}