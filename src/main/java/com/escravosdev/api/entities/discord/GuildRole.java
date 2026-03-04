package com.escravosdev.api.entities.discord;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name="guild_roles")
@Setter
@Getter
@NoArgsConstructor
public class GuildRole {
    @Id
    private String id;

    private String name;
    private Integer position;   // prioridade — quanto maior, mais alto
    private String color;       // hex — vem da API automaticamente
    private String gradient;    // preenchido manualmente pelo adm pras roles especiais
    private String iconUrl;

    // essa role importa pro sistema?
    private boolean functional = false;

    private Instant updatedAt;
}
