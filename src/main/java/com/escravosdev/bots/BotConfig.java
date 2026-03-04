package com.escravosdev.bots;

import com.escravosdev.api.entities.discord.DiscordProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final DiscordProperties props;

    @Bean
    public JDA jda(BotReadyListener botReadyListener) throws InterruptedException {
        log.info("Iniciando bot...");

        return JDABuilder.createDefault(props.botToken())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES
                )
                .addEventListeners(botReadyListener)
                .build()
                .awaitReady();
    }
}
