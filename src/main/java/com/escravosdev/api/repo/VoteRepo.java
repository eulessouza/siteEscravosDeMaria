package com.escravosdev.api.repo;

import com.escravosdev.api.entities.Vote;
import com.escravosdev.api.entities.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepo extends JpaRepository<Vote, UUID> {

    Optional<Vote> findByUserIdAndPostId(UUID userId, UUID postId);
    Optional<Vote> findByUserIdAndCommentId(UUID userId, UUID commentId);

    long countByPostIdAndType(UUID postId, VoteType type);
    long countByCommentIdAndType(UUID commentId, VoteType type);
}