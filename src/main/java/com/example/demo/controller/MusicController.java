package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.YouTubeMusicService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private YouTubeMusicService youTubeMusicService;

    // In-memory music queue (in production, use Redis or database)
    private static final List<Map<String, Object>> musicQueue = Collections.synchronizedList(new ArrayList<>());
    private static int currentIndex = -1;

    @GetMapping("/search")
    public ResponseEntity<?> searchMusic(@RequestParam String q, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Check if user is admin or super admin
        User user = this.userRepository.findByUsername(username);
        if (user == null || (user.getUserRole() != UserRole.ADMIN && user.getUserRole() != UserRole.SUPER_ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can search for music"));
        }

        try {
            // Use Bloomee-style YouTube search via YouTubeMusicService
            List<Map<String, Object>> results = this.youTubeMusicService.searchMusic(q);
            return ResponseEntity.ok(Map.of("results", results));
        } catch (Exception e) {
            System.err.println("Music search error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/queue")
    public ResponseEntity<?> getQueue(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        return ResponseEntity.ok(Map.of(
            "queue", new ArrayList<>(musicQueue),
            "currentIndex", currentIndex
        ));
    }

    @PostMapping("/queue/add")
    public ResponseEntity<?> addToQueue(@RequestBody Map<String, Object> track, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Check if user is admin or super admin
        User user = this.userRepository.findByUsername(username);
        if (user == null || (user.getUserRole() != UserRole.ADMIN && user.getUserRole() != UserRole.SUPER_ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can add songs to queue"));
        }

        synchronized (musicQueue) {
            musicQueue.add(track);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Song added to queue"));
    }

    @PostMapping("/queue/remove")
    public ResponseEntity<?> removeFromQueue(@RequestBody Map<String, Object> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Check if user is admin or super admin
        User user = this.userRepository.findByUsername(username);
        if (user == null || (user.getUserRole() != UserRole.ADMIN && user.getUserRole() != UserRole.SUPER_ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can remove songs from queue"));
        }

        Integer index = (Integer) request.get("index");
        if (index == null || index < 0 || index >= musicQueue.size()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid index"));
        }

        synchronized (musicQueue) {
            musicQueue.remove(index.intValue());
            if (currentIndex >= index) {
                currentIndex--;
            }
            if (currentIndex < 0 && musicQueue.size() > 0) {
                currentIndex = 0;
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Song removed from queue"));
    }

    @PostMapping("/queue/play-next")
    public ResponseEntity<?> playNext(@RequestBody Map<String, Object> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Check if user is admin or super admin
        User user = this.userRepository.findByUsername(username);
        if (user == null || (user.getUserRole() != UserRole.ADMIN && user.getUserRole() != UserRole.SUPER_ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can control playback"));
        }

        Integer index = (Integer) request.get("index");
        if (index == null || index < 0 || index >= musicQueue.size()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid index"));
        }

        synchronized (musicQueue) {
            // Move the selected track to be next
            if (index != currentIndex + 1) {
                Map<String, Object> track = musicQueue.remove(index.intValue());
                int insertIndex = currentIndex + 1;
                if (insertIndex > musicQueue.size()) {
                    musicQueue.add(track);
                } else {
                    musicQueue.add(insertIndex, track);
                }
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Song moved to play next"));
    }
}

