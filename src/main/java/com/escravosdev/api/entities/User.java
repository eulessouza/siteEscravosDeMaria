package com.escravosdev.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {
    @Id
    private String discordId; // PK é o ID do Discord direto

    private String username;
    private String email;
    private String avatarHash;

    @CreationTimestamp
    private Instant createdAt;
    private Instant lastLogin;
}
