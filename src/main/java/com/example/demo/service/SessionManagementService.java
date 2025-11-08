package com.example.demo.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

/**
 * Service to track and manage browser sessions to prevent multiple users in same browser
 * Allows same user to login from multiple browsers/devices
 */
@Service
public class SessionManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManagementService.class);
    
    // Maps username to Set of session IDs (allows multiple browsers per user)
    private final ConcurrentHashMap<String, Set<String>> userToSessionsMap = new ConcurrentHashMap<>();
    // Maps session ID to username  
    private final ConcurrentHashMap<String, String> sessionToUserMap = new ConcurrentHashMap<>();
    
    /**
     * Register a new browser session for user (allows multiple browsers per user)
     * @param username the username
     * @param session the HTTP session
     * @return true if session was registered successfully
     */
    public boolean registerUserSession(String username, HttpSession session) {
        String sessionId = session.getId();
        
        // Add this session to user's session set
        this.userToSessionsMap.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        this.sessionToUserMap.put(sessionId, username);
        
        logger.info("Registered browser session {} for user {} (total sessions: {})", 
                   sessionId, username, this.userToSessionsMap.get(username).size());
        return true;
    }
    
    /**
     * Remove all sessions for a user (not used in browser-specific approach)
     * @param username the username
     */
    public void removeUserSession(String username) {
        Set<String> sessions = this.userToSessionsMap.remove(username);
        if (sessions != null) {
            for (String sessionId : sessions) {
                this.sessionToUserMap.remove(sessionId);
            }
            logger.info("Removed all {} sessions for user {}", sessions.size(), username);
        }
    }
    
    /**
     * Remove specific session by session ID
     * @param sessionId the session ID
     */
    public void removeSession(String sessionId) {
        String username = this.sessionToUserMap.remove(sessionId);
        if (username != null) {
            Set<String> userSessions = this.userToSessionsMap.get(username);
            if (userSessions != null) {
                userSessions.remove(sessionId);
                if (userSessions.isEmpty()) {
                    this.userToSessionsMap.remove(username);
                }
            }
            logger.info("Removed session {} for user {} (remaining sessions: {})", 
                       sessionId, username, userSessions != null ? userSessions.size() : 0);
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
     * Check if user has any active sessions (multiple browsers allowed)
     * @param username the username
     * @return true if user has at least one active session
     */
    public boolean hasActiveSession(String username) {
        Set<String> sessions = this.userToSessionsMap.get(username);
        return sessions != null && !sessions.isEmpty();
    }
    
    /**
     * Get first active session ID for user (may have multiple)
     * @param username the username
     * @return session ID or null
     */
    public String getActiveSessionId(String username) {
        Set<String> sessions = this.userToSessionsMap.get(username);
        return sessions != null && !sessions.isEmpty() ? sessions.iterator().next() : null;
    }
    
    /**
     * Get all active session IDs for user
     * @param username the username
     * @return Set of session IDs
     */
    public Set<String> getAllSessionIds(String username) {
        return this.userToSessionsMap.getOrDefault(username, ConcurrentHashMap.newKeySet());
    }
    
    /**
     * Get count of active sessions for user
     * @param username the username
     * @return number of active sessions
     */
    public int getSessionCount(String username) {
        Set<String> sessions = this.userToSessionsMap.get(username);
        return sessions != null ? sessions.size() : 0;
    }
    
    /**
     * Check if user is currently online
     * @param username the username
     * @return true if user has active sessions
     */
    public boolean isUserOnline(String username) {
        return this.hasActiveSession(username);
    }
    
    /**
     * Get total count of online users
     * @return number of users with active sessions
     */
    public long getOnlineUserCount() {
        return this.userToSessionsMap.size();
    }
    
    /**
     * Remove all sessions for a user (for admin actions like ban)
     * @param username the username
     */
    public void removeUserSessions(String username) {
        this.removeUserSession(username);
    }
}