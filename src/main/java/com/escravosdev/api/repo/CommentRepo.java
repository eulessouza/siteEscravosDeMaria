package com.escravosdev.api.repo;

import com.escravosdev.api.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, UUID> {

    @Query("""
        SELECT DISTINCT c FROM Comment c
        LEFT JOIN FETCH c.author
        LEFT JOIN FETCH c.parent
        WHERE c.post.id = :postId
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findByPostIdFetched(@Param("postId") UUID postId);
}