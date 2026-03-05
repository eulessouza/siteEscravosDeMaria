package com.escravosdev.api.dtos.request;

import java.util.List;

public record UpdateBlogPostRequest(
        String title,
        String content,
        String coverImageUrl,
        List<String> imageUrls,
        List<String> tagSlugs,
        String categorySlug,
        boolean publishToSite,
        boolean publishToDiscord,
        boolean publishToInstagram
) {
}
