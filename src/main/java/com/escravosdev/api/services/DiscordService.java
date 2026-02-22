package com.escravosdev.api.services;

import com.escravosdev.api.entities.DiscordProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiscordService {
    private final DiscordProperties props;
    private final RestClient restClient = RestClient.create();

    private static final String DISCORD_API = "https://discord.com/api/v10";
    private static final String TOKEN_URL = "https://discord.com/api/oauth2/token";

    // Monta a URL que o usuário vai acessar pra logar
    public String buildAuthorizationUrl(String redirectPath) {
        var state = URLEncoder.encode(redirectPath != null ? redirectPath : "/", StandardCharsets.UTF_8);

        return "https://discord.com/api/oauth2/authorize" +
                "?client_id=" + props.clientId() +
                "&redirect_uri=" + URLEncoder.encode(props.redirectUri(), StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=identify%20email%20guilds.members.read" +
                "&state="+state;
    }

    // Troca o code pelo access token do Discord
    public String exchangeCodeForToken(String code) {
        var response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=" + props.clientId() +
                        "&client_secret=" + props.clientSecret() +
                        "&grant_type=authorization_code" +
                        "&code=" + code +
                        "&redirect_uri=" + URLEncoder.encode(props.redirectUri(), StandardCharsets.UTF_8))
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }

    // Busca os dados do usuário com o token dele
    public Map<String, Object> fetchUser(String accessToken) {
        return restClient.get()
                .uri(DISCORD_API + "/users/@me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
    }

    // Busca as roles do usuário no SEU servidor — usa o Bot Token
    public List<String> fetchUserRoles(String userId) {
        try {
            var member = restClient.get()
                    .uri(DISCORD_API + "/guilds/" + props.guildId() + "/members/" + userId)
                    .header("Authorization", "Bot " + props.botToken())
                    .retrieve()
                    .body(Map.class);

            return (List<String>) member.get("roles");
        } catch (Exception e) {
            // Usuário não está no servidor
            return List.of();
        }
    }
}
