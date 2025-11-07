package com.example.demo.controller;

import java.time.LocalDateTime;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public Message sendMessage(Message incoming) {
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
        toSave.setTimestamp(LocalDateTime.now());
        Message saved = this.messageRepository.save(toSave);
        return saved;
    }

    // REST: recent chat history for initial page load
    @GetMapping("/api/messages/recent")
    @ResponseBody
    public List<Message> recentMessages(@RequestParam(name = "limit", required = false) Integer limit) {
        List<Message> latest = this.messageRepository.findTop50ByOrderByTimestampDesc();
        Collections.reverse(latest); // oldest first for UI
        if (limit == null || limit <= 0 || limit >= latest.size()) return latest;
        return latest.subList(latest.size() - limit, latest.size());
    }
}
