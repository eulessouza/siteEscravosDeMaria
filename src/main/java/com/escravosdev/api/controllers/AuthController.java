package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.DiscordProperties;
import com.escravosdev.api.entities.DiscordUser;
import com.escravosdev.api.services.DiscordService;
import com.escravosdev.api.services.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor public class AuthController {

    private final DiscordService discordService;
    private final JwtService jwtService;
    private final DiscordProperties props;

    // Frontend chama isso pra iniciar o login
    @Operation(summary = "Login com Discord", description = "Retorna a URL de autorização do Discord")
    @GetMapping("/discord")
    public ResponseEntity<Map<String, Object>> getAuthUrl(
            @RequestParam(required = false, defaultValue="/") String redirect
    ) {
        return ResponseEntity.ok(Map.of("url", discordService.buildAuthorizationUrl(redirect)));
    }
//    public ResponseEntity<Void> redirectToDiscord() {
//        return ResponseEntity.status(HttpStatus.FOUND)
//                .header("Location", discordService.buildAuthorizationUrl())
//                .build();
//    }

    // Discord redireciona aqui após o usuário autorizar
    @Operation(summary = "Callback do Discord", description = "Troca o code pelo JWT")
    @GetMapping("/discord/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestParam String code,
            @RequestParam(required = false, defaultValue = "/") String state
    ) {
        var accessToken = discordService.exchangeCodeForToken(code);
        var userData = discordService.fetchUser(accessToken);

        var userId = (String) userData.get("id");
        var roles = discordService.fetchUserRoles(userId);

        var user = new DiscordUser(
                userId,
                (String) userData.get("username"),
                (String) userData.get("email"),
                (String) userData.get("avatar"),
                roles
        );

        var jwt = jwtService.generateToken(user);
        var redirectPath = URLDecoder.decode(state, StandardCharsets.UTF_8);

        var cookie = ResponseCookie.from("auth_token", jwt)
                .httpOnly(true)
                .secure(true)        // muda pra true quando for HTTPS em produção
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.LOCATION, props.frontendUrl() + redirectPath)
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
                "id", claims.getSubject(),
                "username", claims.get("username"),
                "email", claims.get("email"),
                "roles", claims.get("roles")
        ));
    }
}
