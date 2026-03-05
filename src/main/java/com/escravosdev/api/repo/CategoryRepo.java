package com.escravosdev.api.repo;

import com.escravosdev.api.entities.Category;
import com.escravosdev.api.entities.enums.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepo extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByType(PostType type);
}