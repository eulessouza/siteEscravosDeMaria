package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.discord.*;
import com.escravosdev.api.entities.User;
import com.escravosdev.api.repo.GuildRoleRepo;
import com.escravosdev.api.repo.UserRepo;
import com.escravosdev.api.repo.UserRoleRepo;
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
    private final JwtService jwtService;
    private final DiscordProperties props;
    private final UserRepo userRepo;
    private final GuildRoleRepo guildRoleRepo;
    private final UserRoleRepo userRoleRepo;

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
        var decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        var parts = decoded.split("\\|", 2);
        var redirectPath = parts[0];
        var csrfToken = parts[1];

        // valida o token e remove (one-time use)
        var timestamp = csrfTokens.remove(csrfToken);
        if (timestamp == null || System.currentTimeMillis() - timestamp > 300_000) { // 5 min
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var accessToken = this.discordService.exchangeCodeForToken(code);
        var userData = this.discordService.fetchUser(accessToken);
        var userId = (String) userData.get("id");

        var user = this.userRepo.findByDiscordId(userId).orElse(new User());
        user.setDiscordId(userId);
        user.setUsername((String) userData.get("username"));
        user.setGlobalName((String) userData.get("global_name"));
        user.setEmail((String) userData.get("email"));
        user.setAvatarHash((String) userData.get("avatar"));
        user.setLastLogin(Instant.now());

        var allRoleIds = discordService.fetchUserRoles(userId);
        var guildRoles = guildRoleRepo.findAllById(allRoleIds); // busca as que estão no banco
        var topColorRole = guildRoles.stream()
                .filter(r -> r.getColor() != null)
                .max(Comparator.comparingInt(GuildRole::getPosition))
                .orElse(null);
        if (topColorRole != null) {
            user.setDisplayColor(topColorRole.getGradient() != null
                    ? topColorRole.getGradient()
                    : topColorRole.getColor());
        }

        user.setGender(DiscordProfileRoles.extractGender(allRoleIds));
        user.setReligion(DiscordProfileRoles.extractReligion(allRoleIds));
        userRepo.save(user);

        userRoleRepo.deleteByUser(user);
        var userRoles = guildRoles.stream()
                .filter(GuildRole::isFunctional)
                .map(role -> {
                    var ur = new UserRole();
                    ur.setUser(user);
                    ur.setRole(role);
                    return ur;
                }).toList();
        userRoleRepo.saveAll(userRoles);

        var functionalRoleIds = userRoles.stream()
                .map(ur -> ur.getRole().getId())
                .toList();

        var discordUser = new DiscordUser(
                userId, user.getUsername(), user.getGlobalName(),
                user.getEmail(), user.getAvatarHash(), functionalRoleIds
        );

        var jwt = this.jwtService.generateToken(discordUser);

        // token na URL pro front capturar
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
                "avatar",       buildAvatarUrl(claims),
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
        return "https://cdn.discordapp.com/avatars/" + id + "/" + hash + ".png";
    }
}
