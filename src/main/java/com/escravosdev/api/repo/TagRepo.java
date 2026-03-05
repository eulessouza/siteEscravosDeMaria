package com.escravosdev.api.repo;

import com.escravosdev.api.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepo extends JpaRepository<Tag, UUID> {
    Optional<Tag> findBySlug(String slug);
    Set<Tag> findBySlugIn(List<String> slugs);
}