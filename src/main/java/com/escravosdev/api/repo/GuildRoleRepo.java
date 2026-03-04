package com.escravosdev.api.repo;

import com.escravosdev.api.entities.discord.GuildRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildRoleRepo extends JpaRepository<GuildRole, String> {}
