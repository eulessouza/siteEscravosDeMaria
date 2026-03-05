package com.escravosdev.api.dtos.response;

import com.escravosdev.api.entities.Comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentId,
        UserResponse author,
        String content,
        List<CommentResponse> replies,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        boolean deleted = comment.isDeleted();
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                deleted ? null : UserResponse.from(comment.getAuthor()),
                deleted ? "[deletado]" : comment.getContent(),
                replies,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    private static UserResponse deletedUser() {
        return new UserResponse(
                null,
                "[deletado]",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}