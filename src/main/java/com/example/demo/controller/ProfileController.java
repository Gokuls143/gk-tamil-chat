package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getProfile(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = this.userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("username", user.getUsername());
        profileData.put("email", user.getEmail());
        profileData.put("gender", user.getGender());
        profileData.put("age", user.getAge());
        profileData.put("status", user.getStatus());
        profileData.put("description", user.getDescription());
        profileData.put("story", user.getStory());
        profileData.put("profilePicture", user.getProfilePicture());
        profileData.put("userRole", user.getUserRole() != null ? user.getUserRole().name() : null);

        return ResponseEntity.ok(profileData);
    }

    @PostMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User user = this.userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Update profile fields
        if (profileData.containsKey("status")) {
            String status = profileData.get("status");
            if (status != null && status.length() > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status must be 100 characters or less"));
            }
            user.setStatus(status);
        }

        if (profileData.containsKey("description")) {
            String description = profileData.get("description");
            if (description != null && description.length() > 500) {
                return ResponseEntity.badRequest().body(Map.of("error", "Description must be 500 characters or less"));
            }
            user.setDescription(description);
        }

        if (profileData.containsKey("story")) {
            String story = profileData.get("story");
            if (story != null && story.length() > 1000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Story must be 1000 characters or less"));
            }
            user.setStory(story);
        }

        if (profileData.containsKey("profilePicture")) {
            user.setProfilePicture(profileData.get("profilePicture"));
        }

        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}
