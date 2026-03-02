package com.escravosdev.api.config;

import com.escravosdev.api.entities.DiscordRoles;
import com.escravosdev.api.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        var authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null) {
            try {
                var claims = jwtService.validateAndParse(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        claims, null, extractAuthorities(claims)
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (DiscordRoles.isAdm(claims)) authorities.add(new SimpleGrantedAuthority("ROLE_ADM"));
        if (DiscordRoles.isOrientador(claims)) authorities.add(new SimpleGrantedAuthority("ROLE_ORIENTADOR"));

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return authorities;
    }
}