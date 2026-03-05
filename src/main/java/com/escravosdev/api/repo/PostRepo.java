package com.escravosdev.api.repo;

import com.escravosdev.api.entities.Post;
import com.escravosdev.api.entities.enums.PostStatus;
import com.escravosdev.api.entities.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostRepo extends JpaRepository<Post, UUID> {
    Page<Post> findByTypeAndStatus(PostType type, PostStatus status, Pageable pageable);
    Page<Post> findByStatus(PostStatus status, Pageable pageable);
    @Query("""
    SELECT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.status = :status
""")
    List<Post> findByStatusWithDetails(@Param("status") PostStatus status);
    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.status = :status
""")
    List<Post> findByStatusFetched(@Param("status") PostStatus status);

    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.type = :type AND p.status = :status
""")
    List<Post> findByTypeAndStatusFetched(@Param("type") PostType type, @Param("status") PostStatus status);

    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.status IN :statuses
""")
    List<Post> findByStatusInFetched(@Param("statuses") List<PostStatus> statuses);
}