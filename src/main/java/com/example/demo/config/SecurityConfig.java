package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // TOTALLY DISABLE CSRF (required for frontend JSON login)
            .csrf(csrf -> csrf.disable())

            // Allow login/register without authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/login",
                    "/api/register",
                    "/api/users/exists",
                    "/api/forgot-password",
                    "/api/forgot-username",
                    "/index.html",
                    "/",
                    "/static/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            // No HTTP Basic
            .httpBasic(basic -> basic.disable())

            // Allow all CORS
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                config.setAllowCredentials(true);
                return config;
            }));

        return http.build();
    }
}
