package com.escravosdev.api.config;

import com.escravosdev.api.entities.discord.DiscordProperties;
import com.escravosdev.api.entities.discord.GuildRole;
import com.escravosdev.api.repo.CsrfTokenRepo;
import com.escravosdev.api.repo.GuildRoleRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildRoleCache {

    private final GuildRoleRepo guildRoleRepo;
    private final DiscordProperties props;
    private final RestClient restClient = RestClient.create();

    private final CsrfTokenRepo csrfTokenRepo;

    private static final String DISCORD_API = "https://discord.com/api/v10";
    private static final String CDN = "https://cdn.discordapp.com";

    @Transactional
    @Scheduled(fixedRateString = "PT30M")
    public void sync() {
        log.info("Sincronizando roles do servidor via API REST...");
        try {
            List<Map<String, Object>> roles = restClient.get()
                    .uri(DISCORD_API + "/guilds/" + props.guildId() + "/roles")
                    .header("Authorization", "Bot " + props.botToken())
                    .retrieve()
                    .body(List.class);

            if (roles == null || roles.isEmpty()) {
                log.error("Nenhuma role retornada pela API");
                return;
            }

            for (var roleData : roles) {
                var id       = (String) roleData.get("id");
                var name     = (String) roleData.get("name");
                var position = (Integer) roleData.get("position");
                var colorInt = (Integer) roleData.get("color");
                var iconHash = (String) roleData.get("icon");

                // gradiente nativo do Discord
                String gradient = null;
                var colors = (Map<String, Object>) roleData.get("colors");
                if (colors != null) {
                    var secondary = (Integer) colors.get("secondary_color");
                    if (secondary != null) {
                        var primary   = (Integer) colors.get("primary_color");
                        var tertiary  = (Integer) colors.get("tertiary_color");
                        var c1 = String.format("#%06x", primary);
                        var c2 = String.format("#%06x", secondary);
                        gradient = tertiary != null
                                ? String.format("linear-gradient(135deg, %s, %s, %s)",
                                c1, c2, String.format("#%06x", tertiary))
                                : String.format("linear-gradient(135deg, %s, %s)", c1, c2);
                    }
                }

                var role = guildRoleRepo.findById(id).orElse(new GuildRole());
                role.setId(id);
                role.setName(name);
                role.setPosition(position);
                role.setColor(colorInt != null && colorInt != 0
                        ? String.format("#%06x", colorInt) : null);
                role.setIconUrl(iconHash != null
                        ? CDN + "/role-icons/" + id + "/" + iconHash + ".png"
                        : null);

                // só sobrescreve gradient se vier da API — não apaga o que foi definido manualmente
                if (gradient != null) role.setGradient(gradient);

                role.setUpdatedAt(Instant.now());
                guildRoleRepo.save(role);

                log.info("[{}] {} | cor: {} | gradiente: {} | ícone: {}",
                        id, name,
                        role.getColor() != null ? role.getColor() : "sem cor",
                        gradient != null ? gradient : "sem gradiente",
                        iconHash != null ? "sim" : "não");
            }

            log.info("Sync concluído — {} roles salvas.", roles.size());
        } catch (Exception e) {
            log.error("Erro ao sincronizar roles: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredCsrf() {
        csrfTokenRepo.deleteByCreatedAtBefore(Instant.now().minus(Duration.ofMinutes(31)));
    }
}