package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.DiscordProperties;
import com.escravosdev.api.entities.DiscordRoles;
import com.escravosdev.api.entities.DiscordUser;
import com.escravosdev.api.entities.User;
import com.escravosdev.api.repo.UserRepo;
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

        var user = this.userRepo.findById(userId).orElse(new User());
        user.setDiscordId(userId);
        user.setUsername((String) userData.get("username"));
        user.setEmail((String) userData.get("email"));
        user.setAvatarHash((String) userData.get("avatar"));
        user.setLastLogin(Instant.now());
        this.userRepo.save(user);

        var roles = this.discordService.fetchUserRoles(userId);
        var discordUser = new DiscordUser(
                userId,
                user.getUsername(),
                user.getEmail(),
                user.getAvatarHash(),
                roles
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

        return ResponseEntity.ok(Map.of(
                "id",           claims.getSubject(),
                "username",     claims.get("username"),
                "email",        claims.get("email"),
                "avatar",       buildAvatarUrl(claims),
                "roles",        claims.get("roles"),
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
