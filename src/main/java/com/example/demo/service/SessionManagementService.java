package com.example.demo.service;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

/**
 * Service to track and manage user sessions to prevent multiple logins
 */
@Service
public class SessionManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);
    
    // Maps username to session ID
    private final ConcurrentHashMap<String, String> userToSessionMap = new ConcurrentHashMap<>();
    // Maps session ID to username  
    private final ConcurrentHashMap<String, String> sessionToUserMap = new ConcurrentHashMap<>();
    
    /**
     * Register a new user session
     * @param username the username
     * @param session the HTTP session
     * @return true if session was registered, false if user already has active session
     */
    public boolean registerUserSession(String username, HttpSession session) {
        String sessionId = session.getId();
        
        // Check if user already has an active session
        String existingSessionId = this.userToSessionMap.get(username);
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            logger.info("User {} already has active session {}. New session: {}", 
                       username, existingSessionId, sessionId);
            // Remove the old session mapping
            this.removeUserSession(username);
        }
        
        // Register new session
        this.userToSessionMap.put(username, sessionId);
        this.sessionToUserMap.put(sessionId, username);
        
        logger.info("Registered session {} for user {}", sessionId, username);
        return true;
    }
    
    /**
     * Remove user session mapping
     * @param username the username
     */
    public void removeUserSession(String username) {
        String sessionId = this.userToSessionMap.remove(username);
        if (sessionId != null) {
            this.sessionToUserMap.remove(sessionId);
            logger.info("Removed session mapping for user {}", username);
        }
    }
    
    /**
     * Remove session by session ID
     * @param sessionId the session ID
     */
    public void removeSession(String sessionId) {
        String username = this.sessionToUserMap.remove(sessionId);
        if (username != null) {
            this.userToSessionMap.remove(username);
            logger.info("Removed session {} for user {}", sessionId, username);
        }
    }
    
    /**
     * Get username for session
     * @param sessionId the session ID
     * @return username or null
     */
    public String getUserForSession(String sessionId) {
        return this.sessionToUserMap.get(sessionId);
    }
    
    /**
     * Check if user has active session
     * @param username the username
     * @return true if user has active session
     */
    public boolean hasActiveSession(String username) {
        return this.userToSessionMap.containsKey(username);
    }
    
    /**
     * Get active session ID for user
     * @param username the username
     * @return session ID or null
     */
    public String getActiveSessionId(String username) {
        return this.userToSessionMap.get(username);
    }
}