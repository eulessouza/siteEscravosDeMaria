package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.discord.*;
import com.escravosdev.api.entities.User;
import com.escravosdev.api.repo.GuildRoleRepo;
import com.escravosdev.api.repo.UserRepo;
import com.escravosdev.api.repo.UserRoleRepo;
import com.escravosdev.api.services.AuthService;
import com.escravosdev.api.services.DiscordService;
import com.escravosdev.api.services.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DiscordService discordService;
    private final AuthService authService;
    private final DiscordProperties props;
    private final UserRepo userRepo;

    private final Map<String, Long> csrfTokens = new ConcurrentHashMap<>();

    // Frontend chama isso pra iniciar o login
    @Operation(summary = "Login com Discord", description = "Retorna a URL de autorização do Discord")
    @GetMapping("/discord")
    public ResponseEntity<Map<String, Object>> getAuthUrl(
            @RequestParam(required = false, defaultValue="/") String redirect
    ) {
        var csrfToken = UUID.randomUUID().toString();
        this.csrfTokens.put(csrfToken, System.currentTimeMillis()); // guarda pra validar no callback

        return ResponseEntity.ok(Map.of("url", this.discordService.buildAuthorizationUrl(redirect, csrfToken)));
    }

    // Discord redireciona aqui após o usuário autorizar
    @Operation(summary = "Callback do Discord", description = "Troca o code pelo JWT")
    @GetMapping("/discord/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "/") String state
    ) {
        var decoded   = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        var parts     = decoded.split("\\|", 2);
        var redirectPath = parts[0];
        var csrfToken    = parts[1];

        var timestamp = csrfTokens.remove(csrfToken);
        if (timestamp == null || System.currentTimeMillis() - timestamp > 300_000) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var jwt = authService.processCallback(code, redirectPath);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, props.frontendUrl() + redirectPath + "?token=" + jwt)
                .build();

    }

    // Rota pra checar quem está logado
    @Operation(summary = "Info do usuário", description = "Retorna as informações do usuário do Discord")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        var claims = (Claims) SecurityContextHolder.getContext().
                getAuthentication()
                .getPrincipal();

        var user = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(Map.of(
                "id",           claims.getSubject(),
                "username",     user.getUsername(),
                "globalName",   user.getGlobalName() != null ? user.getGlobalName() : "",
                "email",        user.getEmail(),
                "avatar",       this.buildAvatarUrl(claims),
                "displayColor", user.getDisplayColor() != null ? user.getDisplayColor() : "#ffffff",
                "gender",       user.getGender() != null ? user.getGender() : "",
                "religion",     user.getReligion() != null ? user.getReligion() : "",
                "permissions", Map.of(
                        "blog",    Map.of("canPost",   DiscordRoles.canCreateBlogPost(claims),
                                "canComment", DiscordRoles.canCommentBlog(claims)),
                        "forum",   Map.of("canPost",   DiscordRoles.canCreateForumPost(claims),
                                "canComment", DiscordRoles.canCommentForum(claims)),
                        "duvidas", Map.of("canPost",   DiscordRoles.canCreateDuvida(claims),
                                "canAnswer",  DiscordRoles.canAnswerDuvida(claims))
                )
        ));
    }

    private String buildAvatarUrl(Claims claims) {
        var id = claims.getSubject();
        var hash = (String) claims.get("avatar");
        if (hash == null || hash.isBlank()) return null;
        var ext = hash.startsWith("a_") ? "gif" : "png";
        return "https://cdn.discordapp.com/avatars/" + id + "/" + hash + "." + ext;

    }
}
