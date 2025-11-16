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
            // completely disable CSRF for REST APIs
            .csrf(csrf -> csrf.disable())

            // allow these APIs without login
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

            // no HTTP Basic auth
            .httpBasic(basic -> basic.disable())

            // allow CORS
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowCredentials(true);
                config.addAllowedOriginPattern("*");
                config.addAllowedHeader("*");
                config.addAllowedMethod("*");
                return config;
            }));

        return http.build();
    }
}
