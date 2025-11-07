package com.example.demo.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpSession;

/**
 * Simple WS handshake guard: allows connections and stores username from
 * HttpSession if present. Guests/anonymous users are also allowed.
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        log.info("WebSocket handshake request from: {}", request.getRemoteAddress());
        if (request instanceof ServletServerHttpRequest servlet) {
            HttpSession session = servlet.getServletRequest().getSession(false);
            if (session != null) {
                Object user = session.getAttribute("username");
                if (user != null) {
                    log.info("Found username in session: {}", user);
                    attributes.put("username", String.valueOf(user));
                } else {
                    log.info("No username in session, allowing anonymous connection");
                }
            } else {
                log.info("No HTTP session found, allowing anonymous connection");
            }
        }
        // Allow all connections (username validation happens on message send if needed)
        log.info("WebSocket handshake approved");
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        // no-op
    }
}
