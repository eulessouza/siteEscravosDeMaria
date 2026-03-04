package com.escravosdev.api.entities;

import com.escravosdev.api.entities.discord.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Setter
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String discordId;

    private String username;   // display name
    private String globalName; // (@username)
    private String email;
    private String avatarHash;

    // visual
    private String displayColor;    // mapeado manualmente
    private String gender;          // mapeado manualmente
    private String religion;        // mapeado manualmente

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> roles = new ArrayList<>();

    // pra ADM
    private boolean banned = false;
    private boolean muted = false;

    @CreationTimestamp
    private Instant createdAt;
    private Instant lastLogin;
}
