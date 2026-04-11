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
import java.util.Optional;
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
    WHERE p.type = :type AND p.status IN :statuses
""")
    List<Post> findByTypeAndStatusInFetched(@Param("type") PostType type, @Param("statuses") List<PostStatus> statuses);

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
    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.id = :id
""")
    Optional<Post> findByIdFetched(@Param("id") UUID id);

    /**
     * Retorna os N posts mais recentes do BLOG já publicados
     * (usado na Home)
     */
    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.type = :type 
      AND p.status = :status
    ORDER BY p.createdAt DESC
""")
    List<Post> findRecentPublished(
            @Param("type") PostType type,
            @Param("status") PostStatus status,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Retorna posts do FÓRUM em trending (ordenado por score de votos)
     * Sem DISTINCT problemático + sem fetch pesado na query principal
     */
    @Query("""
    SELECT p FROM Post p
    WHERE p.type = :type 
      AND p.status = :status
    ORDER BY 
        (COALESCE((SELECT COUNT(v) FROM Vote v WHERE v.post = p AND v.type = 'UPVOTE'), 0) -
         COALESCE((SELECT COUNT(v) FROM Vote v WHERE v.post = p AND v.type = 'DOWNVOTE'), 0)) DESC,
        p.createdAt DESC
""")
    List<Post> findTrendingForum(
            @Param("type") PostType type,
            @Param("status") PostStatus status,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Retorna a última dúvida (QUESTION) que foi respondida (tem comentários)
     * Ordenada pela data da última atualização (idealmente quando recebeu a resposta)
     */
    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN FETCH p.author
    LEFT JOIN FETCH p.category
    LEFT JOIN FETCH p.tags
    LEFT JOIN FETCH p.images
    WHERE p.type = :type 
      AND p.status IN (:statuses)
      AND EXISTS (SELECT 1 FROM Comment c WHERE c.post = p AND c.deleted = false)
    ORDER BY p.updatedAt DESC
""")
    Optional<Post> findLastAnsweredQuestion(
            @Param("type") PostType type,
            @Param("statuses") List<PostStatus> statuses
    );
}