package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Check database connectivity
            this.jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            status.put("status", "UP");
            status.put("database", "Connected");
            status.put("timestamp", System.currentTimeMillis());
            status.put("uploadDir", System.getProperty("java.io.tmpdir") + "uploads/");
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("database", "Connection failed: " + e.getMessage());
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503).body(status);
        }
    }
}