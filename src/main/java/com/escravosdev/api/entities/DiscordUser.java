package com.escravosdev.api.entities;

import java.util.List;

public record DiscordUser (
    String id,
    String username, // username do Discord
    String email, // e-mail que o usuário tem no Discord
    String avatar, // link do avatar dele (CDN do Discord)
    List<String> roles // IDs das roles do servidor
) {}