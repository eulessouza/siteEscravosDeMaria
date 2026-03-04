package com.escravosdev.api.repo;

import com.escravosdev.api.entities.Post;
import com.escravosdev.api.entities.PostStatus;
import com.escravosdev.api.entities.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepo extends JpaRepository<Post, UUID> {
    Page<Post> findByTypeAndStatus(PostType type, PostStatus status, Pageable pageable);
    Page<Post> findByStatus(PostStatus status, Pageable pageable);
}