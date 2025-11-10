package com.example.demo.controller;

import com.example.demo.service.PermissionService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    private PermissionService permissionService;
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
        // For now, return a simple response to fix the frontend loading issue
        // Database transactions are having issues, so we'll implement a workaround
    List<Map<String, Object>> userList = new ArrayList<>();
    try {
            // Try to get users from database
            List<User> allUsers = this.userRepository.findAll();
            
            // Get all connected usernames (includes guests)
            var connectedUsernames = this.userTrackingService.getConnectedUsernames();
            
            log.info("getAllUsers: Found {} registered users, {} connected usernames: {}", 
                     allUsers.size(), connectedUsernames.size(), connectedUsernames);
            
            // Get list of registered usernames
            List<String> registeredUsernames = allUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
            
            log.info("getAllUsers: Registered usernames: {}", registeredUsernames);
            
            // Create response with registered users and their online status
            List<Map<String, Object>> tempUserList = allUsers.stream()
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
            
            userList.addAll(tempUserList);
            
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
        } catch (Exception e) {
            log.error("Error loading users from database: {}", e.getMessage());
            
            // Fallback: Return connected users from tracking service
            var connectedUsernames = this.userTrackingService.getConnectedUsernames();
            List<Map<String, Object>> fallbackList = connectedUsernames.stream()
                .map(username -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("username", username);
                    userMap.put("email", "");
                    userMap.put("online", true);
                    userMap.put("isGuest", true);
                    userMap.put("profilePicture", null);
                    userMap.put("status", null);
                    return userMap;
                })
                .collect(Collectors.toList());
            
            userList.addAll(fallbackList);
            
            // If no connected users, add a sample user to prevent empty state
            if (userList.isEmpty()) {
                Map<String, Object> sampleUser = new HashMap<>();
                sampleUser.put("username", "GuestUser");
                sampleUser.put("email", "");
                sampleUser.put("online", true);
                sampleUser.put("isGuest", true);
                sampleUser.put("profilePicture", null);
                sampleUser.put("status", "Welcome to Tamil Chat!");
                userList.add(sampleUser);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("totalUsers", userList.size());
        response.put("onlineCount", userList.size());
        
        log.info("getAllUsers: Returning {} users in response", userList.size());
        
        return response;
    }

    // Debug endpoint to check user count
    @GetMapping("/api/debug/users")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, Object> debugUsers() {
        List<User> allUsers = this.userRepository.findAll();
        var connectedUsernames = this.userTrackingService.getConnectedUsernames();
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("totalRegisteredUsers", allUsers.size());
        debug.put("connectedUsernames", connectedUsernames);
        debug.put("connectedCount", connectedUsernames.size());
        debug.put("usersList", allUsers.stream().map(User::getUsername).collect(Collectors.toList()));
        
        return debug;
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
        // Permission check: Only MEMBER and above can send links
        if (incoming != null && incoming.getContent() != null && incoming.getContent().matches(".*https?://.*")) {
            User senderUser = this.userRepository.findByUsername(incoming.getSender());
            if (senderUser != null && !this.permissionService.canSendLink(senderUser)) {
                // Optionally log or notify
                log.warn("User {} is not allowed to send links.", incoming.getSender());
                return null;
            }
        }
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
        
        // Handle quoted message information
        if (incoming.getQuotedMessageId() != null) {
            toSave.setQuotedMessageId(incoming.getQuotedMessageId());
            toSave.setQuotedSender(incoming.getQuotedSender());
            toSave.setQuotedContent(incoming.getQuotedContent());
        }
        
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

    // REST: recent chat history for initial page load - HIGHLY OPTIMIZED for Railway
    @GetMapping("/api/messages/recent")
    @ResponseBody
    public List<MessageDTO> recentMessages(@RequestParam(name = "limit", required = false) Integer limit) {
        // Default to 10 messages for fastest loading, max 30 to prevent slow loads
        int messageLimit = (limit != null && limit > 0) ? Math.min(limit, 30) : 10;
        
        try {
            // Use optimized query to get only the messages we need
            List<Message> latest = this.messageRepository.findRecentMessagesLimited(messageLimit);
            Collections.reverse(latest); // oldest first for UI
            
            if (latest.isEmpty()) {
                return new ArrayList<>();
            }
            
            // OPTIMIZATION: Pre-load all users in a single query to avoid N+1 problem
            Set<String> senderNames = latest.stream()
                    .map(Message::getSender)
                    .collect(Collectors.toSet());
            
            List<User> users = this.userRepository.findByUsernameIn(new ArrayList<>(senderNames));
            Map<String, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getUsername, user -> user));
            
            // Convert to MessageDTO with cached user information
            return latest.stream()
                    .map(message -> {
                        User user = userMap.get(message.getSender());
                        return new MessageDTO(message, user);
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error loading recent messages: {}", e.getMessage());
            return new ArrayList<>();
        }
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
