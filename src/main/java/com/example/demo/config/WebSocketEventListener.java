package com.example.demo.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.demo.service.UserTrackingService;

/**
 * Listens for WebSocket connect/disconnect events to track online users.
 */
@Component
public class WebSocketEventListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private UserTrackingService userTrackingService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Try to get username from session attributes
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Object username = (sessionAttributes != null) ? sessionAttributes.get("username") : null;
        
        if (username != null) {
            String user = String.valueOf(username);
            this.userTrackingService.addUser(user);
            log.info("User connected: {} (session: {})", user, sessionId);
        } else {
            log.info("Anonymous user connected (session: {})", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Object username = (sessionAttributes != null) ? sessionAttributes.get("username") : null;
        
        if (username != null) {
            String user = String.valueOf(username);
            this.userTrackingService.removeUser(user);
            log.info("User disconnected: {} (session: {})", user, sessionId);
        } else {
            log.info("Anonymous user disconnected (session: {})", sessionId);
        }
    }
}
