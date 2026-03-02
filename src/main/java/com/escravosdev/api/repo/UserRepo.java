package com.escravosdev.api.repo;

import com.escravosdev.api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {}
