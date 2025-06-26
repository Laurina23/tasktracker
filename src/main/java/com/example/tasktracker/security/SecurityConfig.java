package com.example.tasktracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public class JwtFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtSecretKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();
                    if (username != null) {
                        var auth = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }

                } catch (JwtException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
