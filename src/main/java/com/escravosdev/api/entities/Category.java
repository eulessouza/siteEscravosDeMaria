package com.escravosdev.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // Catequese, Santos, Teologia...

    @Column(nullable = false, unique = true)
    private String slug; // catequese, santos, teologia...

    private String description;

    private String discordChannelId; // ID do canal fórum no servidor

    @Enumerated(EnumType.STRING)
    private PostType type; // BLOG, FORUM, QUESTION, ou null = todos
}