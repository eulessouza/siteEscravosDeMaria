package com.escravosdev.api.controllers;

import com.escravosdev.api.dtos.response.UserResponse;
import com.escravosdev.api.entities.CsrfToken;
import com.escravosdev.api.entities.discord.*;
import com.escravosdev.api.entities.User;
import com.escravosdev.api.repo.CsrfTokenRepo;
import com.escravosdev.api.repo.GuildRoleRepo;
import com.escravosdev.api.repo.UserRepo;
import com.escravosdev.api.repo.UserRoleRepo;
import com.escravosdev.api.services.AuthService;
import com.escravosdev.api.services.DiscordService;
import com.escravosdev.api.services.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DiscordService discordService;
    private final AuthService authService;
    private final DiscordProperties props;
    private final UserRepo userRepo;

    private final CsrfTokenRepo csrfTokenRepo;

    // Frontend chama isso pra iniciar o login
    @Operation(summary = "Login com Discord", description = "Retorna a URL de autorização do Discord")
    @GetMapping("/discord")
    public ResponseEntity<Map<String, Object>> getAuthUrl(
            @RequestParam(required = false, defaultValue="/") String redirect
    ) {
        var csrfToken = UUID.randomUUID().toString();
        var entity = new CsrfToken();
        entity.setToken(csrfToken);
        csrfTokenRepo.save(entity);

        return ResponseEntity.ok(Map.of("url", this.discordService.buildAuthorizationUrl(redirect, csrfToken)));
    }

    // Discord redireciona aqui após o usuário autorizar
    @Operation(summary = "Callback do Discord", description = "Troca o code pelo JWT")
    @GetMapping("/discord/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "/") String state
    ) {
        log.info("Callback recebido — code: {}, state: {}", code, state);
        var decoded   = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        log.info("State decodificado: {}", decoded);

        var parts     = decoded.split("\\|", 2);
        var redirectPath = parts[0];
        var csrfToken    = parts[1];
        log.info("Parts length: {}", parts.length);

        var csrfEntity = csrfTokenRepo.findById(csrfToken).orElse(null);
        if (csrfEntity == null || Duration.between(csrfEntity.getCreatedAt(), Instant.now()).toMinutes() > 30) {
            csrfTokenRepo.deleteById(csrfToken); // limpa se expirado
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        csrfTokenRepo.deleteById(csrfToken); // one-time use

        var jwt = authService.processCallback(code, redirectPath);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, props.frontendUrl() + redirectPath + "?token=" + jwt)
                .build();

    }

    // Rota pra checar quem está logado
    @Operation(summary = "Info do usuário", description = "Retorna as informações do usuário do Discord")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        var claims = getClaims();
        var user = userRepo.findByDiscordId(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return ResponseEntity.ok(Map.of(
                "user",        UserResponse.from(user),
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

    private Claims getClaims() {
        return (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
