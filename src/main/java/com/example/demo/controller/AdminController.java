package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SessionManagementService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SessionManagementService sessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${admin.setup.secret:admin123}")
    private String adminSetupSecret;

    private boolean isAdmin(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return false;
        
        User user = this.userRepository.findByUsername(username);
        return user != null && user.getIsAdmin() != null && user.getIsAdmin();
    }

    @PostMapping("/check-user")
    public ResponseEntity<?> checkUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        User user = this.userRepository.findByEmail(email.toLowerCase().trim());
        
        if (user == null) {
            return ResponseEntity.ok(Map.of(
                "found", false,
                "message", "No account found with this email. Please register first.",
                "email", email
            ));
        }

        return ResponseEntity.ok(Map.of(
            "found", true,
            "username", user.getUsername(),
            "email", user.getEmail(),
            "isAdmin", user.getIsAdmin() != null && user.getIsAdmin(),
            "isBanned", user.getIsBanned() != null && user.getIsBanned(),
            "message", "Account found."
        ));
    }

    @PostMapping("/set-super-admin")
    public ResponseEntity<?> setSuperAdmin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String secretKey = request.get("secret");

        if (!this.adminSetupSecret.equals(secretKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid secret key"));
        }

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        User user = this.userRepository.findByEmail(email.toLowerCase().trim());
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found. Please register first."));
        }

        user.setIsAdmin(true);
        user.setIsSuperAdmin(true);
        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Super admin privileges granted successfully",
            "username", user.getUsername()
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        List<User> allUsers = this.userRepository.findAll();
        List<Map<String, Object>> userList = allUsers.stream().map(u -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", u.getId());
            userInfo.put("username", u.getUsername());
            userInfo.put("email", u.getEmail());
            userInfo.put("age", u.getAge());
            userInfo.put("gender", u.getGender());
            userInfo.put("isAdmin", u.getIsAdmin());
            userInfo.put("isMuted", u.getIsMuted());
            userInfo.put("isBanned", u.getIsBanned());
            userInfo.put("isSuperAdmin", u.getIsSuperAdmin());
            userInfo.put("status", u.getStatus());
            
            boolean isOnline = this.sessionService.isUserOnline(u.getUsername());
            int sessionCount = this.sessionService.getSessionCount(u.getUsername());
            userInfo.put("isOnline", isOnline);
            userInfo.put("sessionCount", sessionCount);
            
            return userInfo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("users", userList);
        result.put("totalUsers", allUsers.size());
        result.put("onlineUsers", userList.stream().mapToInt(u -> (boolean) u.get("isOnline") ? 1 : 0).sum());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        long totalUsers = this.userRepository.count();
        long adminUsers = this.userRepository.countByIsAdminTrue();
        long bannedUsers = this.userRepository.countByIsBannedTrue();
        long mutedUsers = this.userRepository.countByIsMutedTrue();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("bannedUsers", bannedUsers);
        stats.put("mutedUsers", mutedUsers);
        stats.put("onlineUsers", this.sessionService.getOnlineUserCount());
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/promote/{userId}")
    public ResponseEntity<?> promoteUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        user.setIsAdmin(true);
        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User promoted to admin"));
    }

    @PostMapping("/demote/{userId}")
    public ResponseEntity<?> demoteUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        if (user.getIsSuperAdmin() != null && user.getIsSuperAdmin()) {
            return ResponseEntity.badRequest().body("Cannot demote super admin");
        }

        user.setIsAdmin(false);
        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User demoted from admin"));
    }

    @PostMapping("/ban/{userId}")
    public ResponseEntity<?> banUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        if (user.getIsSuperAdmin() != null && user.getIsSuperAdmin()) {
            return ResponseEntity.badRequest().body("Cannot ban super admin");
        }

        user.setIsBanned(true);
        this.userRepository.save(user);
        this.sessionService.removeUserSessions(user.getUsername());
        
        return ResponseEntity.ok(Map.of("success", true, "message", "User banned successfully"));
    }

    @PostMapping("/unban/{userId}")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        user.setIsBanned(false);
        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User unbanned successfully"));
    }

    @PostMapping("/mute/{userId}")
    public ResponseEntity<?> muteUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        if (user.getIsSuperAdmin() != null && user.getIsSuperAdmin()) {
            return ResponseEntity.badRequest().body("Cannot mute super admin");
        }

        user.setIsMuted(true);
        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User muted successfully"));
    }

    @PostMapping("/unmute/{userId}")
    public ResponseEntity<?> unmuteUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        user.setIsMuted(false);
        this.userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User unmuted successfully"));
    }

    @PostMapping("/clear-chat/{userId}")
    public ResponseEntity<?> clearUserChat(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        this.messageRepository.deleteBySender(user.getUsername());

        return ResponseEntity.ok(Map.of("success", true, "message", "User chat cleared successfully"));
    }

    @PostMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpSession session) {
        if (!this.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        Optional<User> userOpt = this.userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOpt.get();
        if (user.getIsSuperAdmin() != null && user.getIsSuperAdmin()) {
            return ResponseEntity.badRequest().body("Cannot delete super admin");
        }

        String deletedUsername = user.getUsername();
        this.sessionService.removeUserSessions(deletedUsername);
        this.messageRepository.deleteBySender(deletedUsername);
        this.userRepository.delete(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
    }
}