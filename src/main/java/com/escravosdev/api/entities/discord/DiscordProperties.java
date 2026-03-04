package com.escravosdev.api.entities.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="discord")
public record DiscordProperties(
        String clientId,
        String clientSecret,
        String botToken,
        String guildId,
        String redirectUri,
        String frontendUrl
) {}
