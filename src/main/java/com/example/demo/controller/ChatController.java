package com.example.demo.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.MessageDTO;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserTrackingService; // user online tracking

@Controller
@RequestMapping
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private UserTrackingService userTrackingService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/online-users")
    @ResponseBody
    public List<User> getOnlineUsers() {
        return this.userTrackingService.getOnlineUsers();
    }

    @GetMapping("/api/users/all")
    @ResponseBody
    public Map<String, Object> getAllUsers() {
        List<User> allUsers = this.userRepository.findAll();
        
        // Get all connected usernames (includes guests)
        var connectedUsernames = this.userTrackingService.getConnectedUsernames();
        
        // Get list of registered usernames
        List<String> registeredUsernames = allUsers.stream()
            .map(User::getUsername)
            .collect(Collectors.toList());
        
        // Create response with registered users and their online status
        List<Map<String, Object>> userList = allUsers.stream()
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("online", connectedUsernames.contains(user.getUsername()));
                userMap.put("isGuest", false);
                userMap.put("profilePicture", user.getProfilePicture());
                userMap.put("status", user.getStatus());
                return userMap;
            })
            .collect(Collectors.toList());
        
        // Add guest users (connected but not registered)
        connectedUsernames.stream()
            .filter(username -> !registeredUsernames.contains(username))
            .forEach(guestUsername -> {
                Map<String, Object> guestMap = new HashMap<>();
                guestMap.put("username", guestUsername);
                guestMap.put("email", "");
                guestMap.put("online", true);
                guestMap.put("isGuest", true);
                userList.add(guestMap);
            });
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("totalUsers", userList.size());
        response.put("onlineCount", connectedUsernames.size());
        
        return response;
    }

    @PostMapping("/api/user/online")
    @ResponseBody
    public Map<String, String> setUserOnline(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username != null && !username.trim().isEmpty()) {
            this.userTrackingService.addUser(username.trim());
            log.info("User marked as online: {}", username);
        }
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return result;
    }

    @PostMapping("/api/user/offline")
    @ResponseBody
    public Map<String, String> setUserOffline(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username != null && !username.trim().isEmpty()) {
            this.userTrackingService.removeUser(username.trim());
            log.info("User marked as offline: {}", username);
        }
        Map<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return result;
    }

    // STOMP endpoint: client sends to /app/sendMessage with a Message payload
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public MessageDTO sendMessage(Message incoming) {
        if (incoming == null) return null;
        // basic normalization: trim content and sender
        String content = incoming.getContent() == null ? null : incoming.getContent().trim();
        String sender = incoming.getSender() == null ? "anonymous" : incoming.getSender().trim();
        if (content == null || content.isEmpty()) {
            // ignore empty messages
            return null;
        }
        Message toSave = new Message();
        toSave.setSender(sender.isEmpty() ? "anonymous" : sender);
        toSave.setContent(content);
        // Use Indian Standard Time (IST) timezone for consistent timestamp handling
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        toSave.setTimestamp(ZonedDateTime.now(istZone).toLocalDateTime());
        Message saved = this.messageRepository.save(toSave);
        
        // Log for debugging timestamp issues
        log.debug("Saving message with timestamp: {} (IST zone: {})", 
                 saved.getTimestamp(), istZone);
        
        // Find user for avatar information
        User user = this.userRepository.findByUsername(saved.getSender());
        
        return new MessageDTO(saved, user);
    }

    // REST: recent chat history for initial page load
    @GetMapping("/api/messages/recent")
    @ResponseBody
    public List<MessageDTO> recentMessages(@RequestParam(name = "limit", required = false) Integer limit) {
        List<Message> latest = this.messageRepository.findTop50ByOrderByTimestampDesc();
        Collections.reverse(latest); // oldest first for UI
        
        // Apply limit if specified
        List<Message> messagesToReturn;
        if (limit == null || limit <= 0 || limit >= latest.size()) {
            messagesToReturn = latest;
        } else {
            messagesToReturn = latest.subList(latest.size() - limit, latest.size());
        }
        
        // Convert to MessageDTO with avatar information
        return messagesToReturn.stream()
                .map(message -> {
                    User user = this.userRepository.findByUsername(message.getSender());
                    return new MessageDTO(message, user);
                })
                .collect(Collectors.toList());
    }

    // REST: Get user profile (read-only)
    @GetMapping("/api/user/profile/{username}")
    @ResponseBody
    public Map<String, Object> getUserProfile(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        
        if (username == null || username.trim().isEmpty()) {
            response.put("error", "Username is required");
            return response;
        }
        
        User user = this.userRepository.findByUsername(username.trim());
        if (user == null) {
            response.put("error", "User not found");
            return response;
        }
        
        // Return safe user information (no password, admin flags, etc.)
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("gender", user.getGender());
        profile.put("age", user.getAge());
        profile.put("status", user.getStatus());
        profile.put("description", user.getDescription());
        profile.put("story", user.getStory());
        profile.put("profilePicture", user.getProfilePicture());
        
        response.put("profile", profile);
        return response;
    }
}
