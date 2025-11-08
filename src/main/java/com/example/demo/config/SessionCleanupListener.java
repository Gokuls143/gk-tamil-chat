package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.service.SessionManagementService;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Session listener to clean up user session mappings when sessions expire
 */
@Component
public class SessionCleanupListener implements HttpSessionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupListener.class);
    
    @Autowired
    private SessionManagementService sessionService;
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.debug("Session created: {}", se.getSession().getId());
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        logger.info("Session destroyed: {}", sessionId);
        
        // Clean up user session mapping
        this.sessionService.removeSession(sessionId);
    }
}