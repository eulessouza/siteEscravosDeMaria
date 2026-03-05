package com.escravosdev.api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "csrf_tokens")
@Getter
@Setter
@NoArgsConstructor
public class CsrfToken {
    @Id
    private String token;
    private Instant createdAt = Instant.now();
}