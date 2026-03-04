package com.escravosdev.api.dtos;

import com.escravosdev.api.entities.Post;
import com.escravosdev.api.entities.PostStatus;
import com.escravosdev.api.entities.PostType;

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
        AuthorResponse author,
        CategoryResponse category,
        List<String> tags,
        boolean publishToSite,
        boolean publishToDiscord,
        boolean publishToInstagram,
        Instant createdAt,
        Instant updatedAt
) {
    public record AuthorResponse(
            String discordId,
            String username,
            String globalName,
            String avatarUrl,
            String displayColor,
            String gender,
            String religion
    ) {}

    public record CategoryResponse(
            UUID id,
            String name,
            String slug
    ) {}

    public static PostResponse from(Post post) {
        var author = new AuthorResponse(
                post.getAuthor().getDiscordId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getGlobalName(),
                buildAvatarUrl(post.getAuthor().getDiscordId(), post.getAuthor().getAvatarHash()),
                post.getAuthor().getDisplayColor(),
                post.getAuthor().getGender(),
                post.getAuthor().getReligion()
        );

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

    private static String buildAvatarUrl(String discordId, String hash) {
        if (hash == null || hash.isBlank()) return null;
        var ext = hash.startsWith("a_") ? "gif" : "png";
        return "https://cdn.discordapp.com/avatars/" + discordId + "/" + hash + "." + ext;
    }

}
