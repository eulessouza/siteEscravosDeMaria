package com.escravosdev.api.dtos.response;

import com.escravosdev.api.entities.Comment;
import com.escravosdev.api.entities.Vote;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentId,
        UserResponse author,
        String content,
        long upvotes,
        long downvotes,
        String userVote,
        List<CommentResponse> replies,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment, List<CommentResponse> replies, VoteResponse votes) {
        boolean deleted = comment.isDeleted();
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                deleted ? null : UserResponse.from(comment.getAuthor()),
                deleted ? "[deletado]" : comment.getContent(),
                votes != null ? votes.upvotes() : 0,
                votes != null ? votes.downvotes() : 0,
                votes != null ? votes.userVote() : null,
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

    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return from(comment, replies, null);
    }
}