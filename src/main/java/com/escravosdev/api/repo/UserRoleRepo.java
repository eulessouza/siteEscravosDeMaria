package com.escravosdev.api.repo;

import com.escravosdev.api.entities.discord.UserRole;
import com.escravosdev.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepo extends JpaRepository<UserRole, UUID> {
    List<UserRole> findByUser(User user);
    void deleteByUser(User user);
}