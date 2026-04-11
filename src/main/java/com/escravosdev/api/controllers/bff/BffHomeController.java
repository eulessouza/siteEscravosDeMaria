package com.escravosdev.api.controllers.bff;

import com.escravosdev.api.dtos.response.HomeData;
import com.escravosdev.api.entities.discord.DiscordProperties;
import com.escravosdev.api.services.BlogService;
import com.escravosdev.api.services.ForumService;
import com.escravosdev.api.services.QuestionService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bff")
@RequiredArgsConstructor
public class BffHomeController {

    private final BlogService blogService;
    private final ForumService forumService;
    private final QuestionService questionService;
    private final JDA jda;
    private final DiscordProperties discordProperties;   // ← injetado corretamente

    @GetMapping("/home")
    public HomeData getHome(@AuthenticationPrincipal Claims claims) {
        var recentBlogPosts = blogService.getRecentPublished(6);
        var trendingForumPosts = forumService.getTrending(4, claims);
        var lastAnsweredQuestion = questionService.getLastAnswered().orElse(null);

        var liturgyToday = getLiturgyToday();
        var prayerOfTheDay = getPrayerOfTheDay();
        var saintToday = getSaintToday();
        var lastPopes = getLastPopes();
        var supporters = getSupporters();

        // Contagem de membros do Discord
        long memberCount = 0;
        try {
            var guild = jda.getGuildById(discordProperties.guildId());
            if (guild != null) {
                memberCount = guild.getMemberCount();   // total de membros (inclui bots)
            }
        } catch (Exception e) {
            // Fallback silencioso - não quebra a Home se o Discord estiver offline
            memberCount = 999; // valor fallback temporário (você pode remover depois)
        }

        return new HomeData(
                liturgyToday,
                prayerOfTheDay,
                saintToday,
                lastPopes,
                recentBlogPosts,
                trendingForumPosts,
                lastAnsweredQuestion,
                supporters,
                memberCount,                    // ← adicionado
                discordProperties.frontendUrl() // ou um link fixo do invite
        );
    }

    // ==================== PLACEHOLDERS ====================
    private Object getLiturgyToday() {
        return Map.of(
                "title", "Quinta-feira da Semana Santa",
                "reading", "Ex 12, 1-8.11-14",
                "color", "Vermelho"
        );
    }

    private Object getPrayerOfTheDay() {
        return Map.of(
                "title", "Oração da Manhã",
                "text", "Ó Jesus, por meio do Imaculado Coração de Maria, concedei-me a graça...",
                "author", "Tradição da Igreja"
        );
    }

    private Object getSaintToday() {
        return Map.of(
                "name", "São Vicente Ferrer",
                "shortBio", "Presbítero e Doutor da Igreja (1350-1419)",
                "feastDate", "05 de abril"
        );
    }

    private List<Map<String, Object>> getLastPopes() {
        return List.of(
                Map.of("name", "Papa Leão XIV", "pontificate", "2025–"),
                Map.of("name", "Papa Francisco", "pontificate", "2013–2025"),
                Map.of("name", "Papa Bento XVI", "pontificate", "2005–2013"),
                Map.of("name", "São João Paulo II", "pontificate", "1978–2005"),
                Map.of("name", "São Paulo VI", "pontificate", "1963–1978")
        );
    }

    private List<Map<String, Object>> getSupporters() {
        return List.of(
                Map.of("name", "Raul", "time", "2 anos"),
                Map.of("name", "Dex", "time", "2 anos"),
                Map.of("name", "Rafael", "time", "1 ano, 6 meses"),
                Map.of("name", "José", "time", "1 ano, 3 meses"),
                Map.of("name", "Victoria", "time", "1 ano")
        );
    }
}