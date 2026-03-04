package com.escravosdev.api.entities.discord;

import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.Set;

public class DiscordRoles {

    public static final String REI             = "1461556222766354483";
    public static final String AUTORIDADE_REAL = "1458280546684768397";
    public static final String DUQUE           = "1456875359860424775";
    public static final String MARQUES         = "1456875744209797131";
    public static final String CONDE           = "1456875804204990637";
    public static final String ORIENTADOR      = "1463258112118100020";
    public static final String ADM_ORACOES     = "1457183789607293010";
    public static final String SENHOR_FEUDAL   = "1457159160574775576";
    public static final String ROLE_SECRETA    = "1470857348556914899";

    private static final List<String> ADM_ROLES = List.of(
            REI, AUTORIDADE_REAL, DUQUE, ROLE_SECRETA, MARQUES, CONDE
    );

    // todas as roles que o sistema conhece
    public static final Set<String> ALL_KNOWN_ROLES = Set.of(
            REI, AUTORIDADE_REAL, DUQUE, ROLE_SECRETA, MARQUES, CONDE,
            ORIENTADOR, ADM_ORACOES, SENHOR_FEUDAL
    );

    private DiscordRoles() {}

    public static boolean isAdm(Claims claims) {
        var roles = (List<?>) claims.get("roles");
        if (roles == null) return false;
        return ADM_ROLES.stream().anyMatch(roles::contains);
    }

    public static boolean isOrientador(Claims claims) {
        var roles = (List<?>) claims.get("roles");
        return roles != null && roles.contains(ORIENTADOR);
    }

    public static boolean hasRole(Claims claims, String roleId) {
        var roles = (List<?>) claims.get("roles");
        return roles != null && roles.contains(roleId);
    }

    // --- blog ---
    public static boolean canCreateBlogPost(Claims claims) {
        return isAdm(claims);
    }
    public static boolean canCommentBlog(Claims claims) {
        return claims != null;
    }

    // --- fórum ---
    public static boolean canCreateForumPost(Claims claims) {
        return claims != null;
    }
    public static boolean canCommentForum(Claims claims) {
        return claims != null;
    }

    // --- dúvidas ---
    public static boolean canCreateDuvida(Claims claims) {
        return claims != null;
    }
    public static boolean canAnswerDuvida(Claims claims) {
        return isOrientador(claims) || isAdm(claims);
    }
}