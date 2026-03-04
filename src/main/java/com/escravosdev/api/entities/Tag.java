package com.escravosdev.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tags")
@Getter @Setter @NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // SANTOS, ORAÇÕES, DISCUSSÃO...

    @Column(nullable = false, unique = true)
    private String slug; // santos, oracoes, discussao...
}