package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/"),
                    new AntPathRequestMatcher("/index.html"),
                    new AntPathRequestMatcher("/landing.html"), // will restrict via websocket/session check in future if needed
                    new AntPathRequestMatcher("/style.css"),
                    new AntPathRequestMatcher("/home.js"),
                    new AntPathRequestMatcher("/script.js"),
                    new AntPathRequestMatcher("/register.js"),
                    new AntPathRequestMatcher("/sounds/**"), // Notification sounds
                    new AntPathRequestMatcher("/chat/**"), // websocket handshake endpoint and SockJS fallbacks
                    new AntPathRequestMatcher("/api/register"),
                    new AntPathRequestMatcher("/api/login"),
                    new AntPathRequestMatcher("/api/logout"),
                    new AntPathRequestMatcher("/api/forgot-username"),
                    new AntPathRequestMatcher("/api/users/exists"),
                    new AntPathRequestMatcher("/api/users/all"),
                    new AntPathRequestMatcher("/api/profile"),
                    new AntPathRequestMatcher("/api/profile/**"),
                    new AntPathRequestMatcher("/profile.html"),
                    new AntPathRequestMatcher("/api/user/online"),
                    new AntPathRequestMatcher("/api/user/offline"),
                    new AntPathRequestMatcher("/api/user/profile/**"), // Profile viewing endpoint
                    new AntPathRequestMatcher("/api/upload"), // File upload endpoint
                    new AntPathRequestMatcher("/uploads/**"), // Static file serving
                    new AntPathRequestMatcher("/api/messages/recent"),
                    new AntPathRequestMatcher("/online-users"),
                    new AntPathRequestMatcher("/app/**"),  // STOMP application destination
                    new AntPathRequestMatcher("/topic/**"), // STOMP broker destination
                    // Admin static files - allow access to admin UI files
                    new AntPathRequestMatcher("/admin/**"),
                    // Admin authentication endpoints - must be accessible to login
                    new AntPathRequestMatcher("/api/admin/login"),
                    new AntPathRequestMatcher("/api/admin/logout"),
                    new AntPathRequestMatcher("/api/admin/session/check")
                ).permitAll()
                // Admin API endpoints - require authentication but handled by controller
                .requestMatchers(
                    new AntPathRequestMatcher("/api/admin/**")
                ).authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .maximumSessions(10) // Allow multiple admin sessions
                .maxSessionsPreventsLogin(false) // Don't prevent new logins
            );

        return http.build();
    }
}
