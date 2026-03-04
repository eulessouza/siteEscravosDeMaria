package com.escravosdev.api.services;

import com.escravosdev.api.entities.User;
import com.escravosdev.api.entities.discord.DiscordProfileRoles;
import com.escravosdev.api.entities.discord.DiscordUser;
import com.escravosdev.api.entities.discord.GuildRole;
import com.escravosdev.api.entities.discord.UserRole;
import com.escravosdev.api.repo.GuildRoleRepo;
import com.escravosdev.api.repo.UserRepo;
import com.escravosdev.api.repo.UserRoleRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final GuildRoleRepo guildRoleRepo;
    private final UserRoleRepo userRoleRepo;
    private final DiscordService discordService;
    private final JwtService jwtService;

    @Transactional
    public String processCallback(String code, String redirectPath) {
        var accessToken = discordService.exchangeCodeForToken(code);
        var userData    = discordService.fetchUser(accessToken);
        var userId      = (String) userData.get("id");

        var user = userRepo.findByDiscordId(userId).orElse(new User());
        user.setDiscordId(userId);
        user.setUsername((String) userData.get("username"));
        user.setGlobalName((String) userData.get("global_name"));
        user.setEmail((String) userData.get("email"));
        user.setAvatarHash((String) userData.get("avatar"));
        user.setLastLogin(Instant.now());

        var allRoleIds = discordService.fetchUserRoles(userId);
        var guildRoles = guildRoleRepo.findAllById(allRoleIds);

        var topColorRole = guildRoles.stream()
                .filter(r -> r.getColor() != null || r.getGradient() != null)
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

        return jwtService.generateToken(discordUser);
    }
}