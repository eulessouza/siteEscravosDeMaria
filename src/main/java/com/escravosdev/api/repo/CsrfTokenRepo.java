package com.escravosdev.api.repo;

import com.escravosdev.api.entities.CsrfToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface CsrfTokenRepo extends JpaRepository<CsrfToken, String> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CsrfToken c WHERE c.createdAt < :threshold")
    void deleteByCreatedAtBefore(Instant threshold);
}
