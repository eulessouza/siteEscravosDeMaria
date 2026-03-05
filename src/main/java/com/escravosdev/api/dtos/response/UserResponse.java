package com.escravosdev.api.dtos.response;

import com.escravosdev.api.entities.User;

public record UserResponse (
        String discordId,
        String username,
        String globalName,
        String email,
        String avatar,
        String displayColor,
        String gender,
        String religion
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getDiscordId(),
                user.getUsername(),
                user.getGlobalName(),
                user.getEmail(),
                buildAvatarUrl(user.getDiscordId(), user.getAvatarHash()),
                user.getDisplayColor(),
                user.getGender(),
                user.getReligion()
        );
    }

    private static String buildAvatarUrl(String discordId, String hash) {
        if (hash == null || hash.isBlank()) return null;
        var ext = hash.startsWith("a_") ? "gif" : "png";
        return "https://cdn.discordapp.com/avatars/" + discordId + "/" + hash + "." + ext;
    }
}
