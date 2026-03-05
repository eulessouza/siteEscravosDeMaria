package com.escravosdev.api.dtos.response;

import com.escravosdev.api.entities.Post;
import com.escravosdev.api.entities.enums.PostStatus;
import com.escravosdev.api.entities.enums.PostType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse (
        UUID id,
        PostType type,
        PostStatus status,
        String title,
        String content,
        String coverImageUrl,
        List<String> imageUrls,
        UserResponse author,
        CategoryResponse category,
        List<String> tags,
        boolean publishToSite,
        boolean publishToDiscord,
        boolean publishToInstagram,
        Instant createdAt,
        Instant updatedAt
) {
    public record CategoryResponse(
            UUID id,
            String name,
            String slug
    ) {}

    public static PostResponse from(Post post) {
        var author = UserResponse.from(post.getAuthor());

        var category = post.getCategory() != null
                ? new CategoryResponse(
                post.getCategory().getId(),
                post.getCategory().getName(),
                post.getCategory().getSlug())
                : null;

        var imageUrls = post.getImages().stream()
                .map(img -> img.getUrl())
                .toList();

        var tagNames = post.getTags().stream()
                .map(tag -> tag.getName())
                .toList();

        return new PostResponse(
                post.getId(),
                post.getType(),
                post.getStatus(),
                post.getTitle(),
                post.getContent(),
                post.getCoverImageUrl(),
                imageUrls,
                author,
                category,
                tagNames,
                post.isPublishToSite(),
                post.isPublishToDiscord(),
                post.isPublishToInstagram(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
