package com.escravosdev.bots;

import com.escravosdev.api.entities.discord.DiscordProperties;
import com.escravosdev.api.entities.discord.GuildRole;
import com.escravosdev.api.repo.GuildRoleRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleSyncService {

    private final GuildRoleRepo guildRoleRepo;
    private final DiscordProperties props;

    private final RestClient restClient = RestClient.create();

    private static final String DISCORD_API = "https://discord.com/api/v10";
    private static final String CDN = "https://cdn.discordapp.com";

    public void sync() {
        log.info("Sincronizando roles via API REST...");

        List<Map<String, Object>> roles = restClient.get()
                .uri(DISCORD_API + "/guilds/" + props.guildId() + "/roles")
                .header("Authorization", "Bot " + props.botToken())
                .retrieve()
                .body(List.class);

        if (roles == null) {
            log.error("Nenhuma role retornada pela API");
            return;
        }

        for (var roleData : roles) {
            var id       = (String) roleData.get("id");
            var name     = (String) roleData.get("name");
            var position = (Integer) roleData.get("position");
            var colorInt = (Integer) roleData.get("color");
            var iconHash = (String) roleData.get("icon");

            // campo colors — gradiente nativo do Discord
            String gradient = null;
            var colors = (Map<String, Object>) roleData.get("colors");
            if (colors != null) {
                var primary   = (Integer) colors.get("primary_color");
                var secondary = (Integer) colors.get("secondary_color");
                var tertiary  = (Integer) colors.get("tertiary_color");

                if (secondary != null) {
                    // tem gradiente real
                    var c1 = String.format("#%06x", primary);
                    var c2 = String.format("#%06x", secondary);
                    gradient = tertiary != null
                            ? String.format("linear-gradient(135deg, %s, %s, %s)",
                            c1, c2, String.format("#%06x", tertiary))
                            : String.format("linear-gradient(135deg, %s, %s)", c1, c2);
                }
            }

            var entity = guildRoleRepo.findById(id).orElse(new GuildRole());
            entity.setId(id);
            entity.setName(name);
            entity.setPosition(position);
            entity.setColor(colorInt != null && colorInt != 0
                    ? String.format("#%06x", colorInt) : null);
            if (gradient != null) {
                entity.setGradient(gradient);
            } else if (entity.getId() == null) {
                // role nova sem gradiente — deixa null
                entity.setGradient(null);
            }
            entity.setIconUrl(iconHash != null
                    ? CDN + "/role-icons/" + id + "/" + iconHash + ".png"
                    : null);
            entity.setUpdatedAt(Instant.now());
            // functional não sobrescreve — definido manualmente
            guildRoleRepo.save(entity);
        }

        log.info("Sync concluído — {} roles salvas.", roles.size());

    }

}
