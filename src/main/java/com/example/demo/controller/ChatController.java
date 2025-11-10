package com.example.demo.controller;

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
import com.example.demo.permissions.Permission;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PermissionService;
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

    @Autowired
    private PermissionService permissionService;

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

                    // Add role information
                    userMap.put("role", user.getUserRole().name());
                    userMap.put("roleDisplayName", user.getUserRole().getDisplayName());
                    userMap.put("roleLevel", user.getUserRole().getLevel());
                    userMap.put("roleColor", user.getUserRole().getColor());
                    userMap.put("roleIcon", user.getUserRole().getIcon());
                    userMap.put("messageCount", user.getMessageCount());
                    userMap.put("isMuted", user.getIsMuted());
                    userMap.put("isBanned", user.getIsBanned());

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
        if (incoming == null) return null;

        // basic normalization: trim content and sender
        String content = incoming.getContent() == null ? null : incoming.getContent().trim();
        String sender = incoming.getSender() == null ? "anonymous" : incoming.getSender().trim();

        if (content == null || content.isEmpty()) {
            // ignore empty messages
            return null;
        }

        // === PERMISSION VALIDATION ===

        // Validate message content based on user permissions
        PermissionService.MessageValidationResult validation = permissionService.validateMessageContent(sender, content);
        if (!validation.isApproved()) {
            log.warn("Message rejected for user {}: {}", sender, validation.getRejectionReason());
            // Create a system message to inform about the restriction
            Message systemMessage = new Message();
            systemMessage.setSender("system");
            systemMessage.setContent(String.format("Message from %s was not sent: %s",
                sender, validation.getRejectionReason()));
            ZoneId istZone = ZoneId.of("Asia/Kolkata");
            systemMessage.setTimestamp(ZonedDateTime.now(istZone).toLocalDateTime());
            Message savedSystem = this.messageRepository.save(systemMessage);
            return new MessageDTO(savedSystem, null);
        }

        // === MESSAGE PROCESSING ===

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

        // === USER ACTIVITY TRACKING ===

        try {
            // Update user activity and message count
            User user = this.userRepository.findByUsername(saved.getSender());
            if (user != null) {
                // Increment message count and update activity
                user.incrementMessageCount();
                user.updateLastActivity();
                this.userRepository.save(user);

                log.debug("Updated user activity for {}: {} messages, last activity: {}",
                         user.getUsername(), user.getMessageCount(), user.getLastActivityAt());
            }
        } catch (Exception e) {
            log.warn("Failed to update user activity for {}: {}", sender, e.getMessage());
        }

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

        // Add role information to profile
        profile.put("role", user.getUserRole().name());
        profile.put("roleDisplayName", user.getUserRole().getDisplayName());
        profile.put("roleLevel", user.getUserRole().getLevel());
        profile.put("roleColor", user.getUserRole().getColor());
        profile.put("roleIcon", user.getUserRole().getIcon());
        profile.put("roleDescription", user.getUserRole().getDescription());
        profile.put("permissions", user.getUserRole().getPermissionNames());
        profile.put("messageCount", user.getMessageCount());
        profile.put("accountAgeDays", user.getAccountAgeInDays());
        profile.put("daysInCurrentRole", user.getDaysInCurrentRole());
        profile.put("roleAssignedAt", user.getRoleAssignedAt());
        profile.put("eligibleForProgression", user.isEligibleForProgression());
        
        response.put("profile", profile);
        return response;
    }

    // === NEW ROLE-BASED ENDPOINTS ===

    /**
     * Check if user can send messages
     */
    @GetMapping("/api/permissions/can-send-messages")
    @ResponseBody
    public Map<String, Object> canSendMessages(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("canSend", permissionService.canSendMessage(username));
        response.put("canSendLinks", permissionService.canSendLinks(username));
        response.put("canUploadImages", permissionService.canUploadImages(username));
        return response;
    }

    /**
     * Delete a message (moderation action)
     */
    @PostMapping("/api/messages/{messageId}/delete")
    @ResponseBody
    public Map<String, Object> deleteMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> payload) {

        String moderatorUsername = payload.get("moderatorUsername");
        Map<String, Object> response = new HashMap<>();

        try {
            // Get the message to be deleted
            Message message = this.messageRepository.findById(messageId).orElse(null);
            if (message == null) {
                response.put("success", false);
                response.put("error", "Message not found");
                return response;
            }

            // Check if moderator can delete this message
            boolean canDelete = permissionService.canDeleteMessage(moderatorUsername, message.getSender());
            if (!canDelete) {
                response.put("success", false);
                response.put("error", "You don't have permission to delete this message");
                return response;
            }

            // Delete the message
            this.messageRepository.delete(message);

            response.put("success", true);
            response.put("message", "Message deleted successfully");
            response.put("deletedBy", moderatorUsername);
            response.put("deletedMessageId", messageId);

            log.info("Message {} deleted by moderator {}", messageId, moderatorUsername);

        } catch (Exception e) {
            log.error("Error deleting message {}: {}", messageId, e.getMessage());
            response.put("success", false);
            response.put("error", "Failed to delete message: " + e.getMessage());
        }

        return response;
    }

    /**
     * Mute a user (moderation action)
     */
    @PostMapping("/api/users/{username}/mute")
    @ResponseBody
    public Map<String, Object> muteUser(
            @PathVariable String username,
            @RequestBody Map<String, String> payload) {

        String moderatorUsername = payload.get("moderatorUsername");
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if moderator can mute this user
            boolean canMute = permissionService.canMuteUser(moderatorUsername, username);
            if (!canMute) {
                response.put("success", false);
                response.put("error", "You don't have permission to mute this user");
                return response;
            }

            // Get target user and mute them
            User targetUser = this.userRepository.findByUsername(username);
            if (targetUser == null) {
                response.put("success", false);
                response.put("error", "User not found");
                return response;
            }

            targetUser.setIsMuted(true);
            this.userRepository.save(targetUser);

            response.put("success", true);
            response.put("message", "User muted successfully");
            response.put("mutedUsername", username);
            response.put("mutedBy", moderatorUsername);

            log.info("User {} muted by moderator {}", username, moderatorUsername);

        } catch (Exception e) {
            log.error("Error muting user {}: {}", username, e.getMessage());
            response.put("success", false);
            response.put("error", "Failed to mute user: " + e.getMessage());
        }

        return response;
    }

    /**
     * Ban a user (admin action)
     */
    @PostMapping("/api/users/{username}/ban")
    @ResponseBody
    public Map<String, Object> banUser(
            @PathVariable String username,
            @RequestBody Map<String, String> payload) {

        String adminUsername = payload.get("adminUsername");
        String reason = payload.getOrDefault("reason", "No reason provided");
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if admin can ban this user
            boolean canBan = permissionService.canBanUser(adminUsername, username);
            if (!canBan) {
                response.put("success", false);
                response.put("error", "You don't have permission to ban this user");
                return response;
            }

            // Get target user and ban them
            User targetUser = this.userRepository.findByUsername(username);
            if (targetUser == null) {
                response.put("success", false);
                response.put("error", "User not found");
                return response;
            }

            targetUser.setIsBanned(true);
            this.userRepository.save(targetUser);

            response.put("success", true);
            response.put("message", "User banned successfully");
            response.put("bannedUsername", username);
            response.put("bannedBy", adminUsername);
            response.put("reason", reason);

            log.warn("User {} banned by admin {} for reason: {}", username, adminUsername, reason);

        } catch (Exception e) {
            log.error("Error banning user {}: {}", username, e.getMessage());
            response.put("success", false);
            response.put("error", "Failed to ban user: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get user's permissions
     */
    @GetMapping("/api/users/{username}/permissions")
    @ResponseBody
    public Map<String, Object> getUserPermissions(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            var permissions = permissionService.getUserPermissions(username);
            var userRole = permissionService.getUserRole(username);

            response.put("username", username);
            response.put("role", userRole.name());
            response.put("roleDisplayName", userRole.getDisplayName());
            response.put("permissions", permissions.stream()
                    .map(Permission::getName)
                    .toList());

            // Include individual permission checks
            response.put("canSendMessages", permissionService.canSendMessage(username));
            response.put("canSendLinks", permissionService.canSendLinks(username));
            response.put("canUploadImages", permissionService.canUploadImages(username));
            response.put("canModerate", permissionService.isModeratorOrHigher(username));
            response.put("canAdministrate", permissionService.isAdminOrHigher(username));
            response.put("isSuperAdmin", permissionService.isSuperAdmin(username));

        } catch (Exception e) {
            log.error("Error getting permissions for user {}: {}", username, e.getMessage());
            response.put("error", "Failed to get permissions: " + e.getMessage());
        }

        return response;
    }
}
