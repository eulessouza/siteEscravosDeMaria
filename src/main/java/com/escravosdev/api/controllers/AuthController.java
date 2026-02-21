package com.escravosdev.api.controllers;

import com.escravosdev.api.entities.DiscordProperties;
import com.escravosdev.api.entities.DiscordUser;
import com.escravosdev.api.services.DiscordService;
import com.escravosdev.api.services.JwtService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor public class AuthController {

    private final DiscordService discordService;
    private final JwtService jwtService;
    private final DiscordProperties props;

    // Frontend chama isso pra iniciar o login
    @GetMapping("/discord")
    public ResponseEntity<Map<String, Object>> getAuthUrl() {
        return ResponseEntity.ok(Map.of("url", discordService.buildAuthorizationUrl()));
    }
//    public ResponseEntity<Void> redirectToDiscord() {
//        return ResponseEntity.status(HttpStatus.FOUND)
//                .header("Location", discordService.buildAuthorizationUrl())
//                .build();
//    }

    // Discord redireciona aqui após o usuário autorizar
    @GetMapping("/discord/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestParam String code) {
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

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "user", Map.of(
                    "id", user.id(),
                    "username", user.username(),
                    "email", user.email(),
                    "roles", user.roles()
                )
        ));
    }

    // Rota pra checar quem está logado
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @RequestHeader("Authorization") String authHeader
    ) {
        var token = authHeader.replace("Bearer ", "");
        var claims = jwtService.validateAndParse(token);
        return ResponseEntity.ok(Map.of(
                "id", claims.getSubject(),
                "username", claims.get("username"),
                "email", claims.get("email"),
                "roles", claims.get("roles")
        ));
    }
}
