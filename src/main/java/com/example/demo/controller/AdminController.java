package com.example.demo.controller;package com.example.demo.controller;



import java.util.*;import java.util.HashMap;

import java.util.stream.Collectors;import java.util.List;

import java.util.Map;

import org.slf4j.Logger;import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.ResponseEntity;import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.web.bind.annotation.DeleteMapping;

import com.example.demo.model.User;import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.UserRepository;import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.service.SessionManagementService;import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api/admin")import com.example.demo.model.User;

public class AdminController {import com.example.demo.repository.MessageRepository;

    import com.example.demo.repository.UserRepository;

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);import com.example.demo.service.UserTrackingService;

    

    @Autowiredimport jakarta.servlet.http.HttpSession;

    private UserRepository userRepository;

    @RestController

    @Autowired@RequestMapping("/api/admin")

    private SessionManagementService sessionService;public class AdminController {

    

    /**    @Autowired

     * Check if current user is admin    private UserRepository userRepository;

     */    

    private boolean isAdmin(HttpSession session) {    @Autowired

        Object username = session.getAttribute("username");    private MessageRepository messageRepository;

        if (username == null) return false;    

            @Autowired

        User user = userRepository.findByUsername(username.toString());    private SimpMessagingTemplate messagingTemplate;

        return user != null && (user.getIsAdmin() || user.getIsSuperAdmin());    

    }    @Autowired

        private UserTrackingService userTrackingService;

    /**    

     * Check if current user is super admin    @Value("${admin.setup.secret:super-secret-default-change-me}")

     */    private String adminSetupSecret;

    private boolean isSuperAdmin(HttpSession session) {    

        Object username = session.getAttribute("username");    // Public diagnostic endpoint to check user status (no authentication needed)

        if (username == null) return false;    @GetMapping("/check-user-status")

            public ResponseEntity<?> checkUserStatus(@RequestParam String email) {

        User user = this.userRepository.findByUsername(this.username.toString());        System.out.println("🔍 User Status Check - Email: " + email);

        return user != null && user.getIsSuperAdmin();        

    }        if (email == null || email.isEmpty()) {

                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));

    /**        }

     * Get all users with admin status and online/offline status        

     */        User user = this.userRepository.findByEmail(email.toLowerCase().trim());

    @GetMapping("/users")        if (user == null) {

    public ResponseEntity<?> getAllUsers(HttpSession session) {            System.out.println("❌ User not found with email: " + email);

        if (!isAdmin(session)) {            return ResponseEntity.status(404).body(Map.of(

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");                "found", false,

        }                "message", "No account found with this email. Please register first.",

                        "email", email

        List<User> allUsers = this.userRepository.findAll();            ));

        List<Map<String, Object>> userList = allUsers.stream().map(user -> {        }

            Map<String, Object> userInfo = new HashMap<>();        

            userInfo.put("id", this.user.getId());        System.out.println("✅ Found user: " + this.user.getUsername() + " | isAdmin: " + this.user.getIsAdmin() + " | isBanned: " + this.user.getIsBanned());

            userInfo.put("username", this.user.getUsername());        

            userInfo.put("email", this.user.getEmail());        return ResponseEntity.ok(Map.of(

            userInfo.put("age", this.user.getAge());            "found", true,

            userInfo.put("gender", this.user.getGender());            "username", this.user.getUsername(),

            userInfo.put("isAdmin", this.user.getIsAdmin());            "email", this.user.getEmail(),

            userInfo.put("isMuted", this.user.getIsMuted());            "isAdmin", this.user.getIsAdmin() != null && this.user.getIsAdmin(),

            userInfo.put("isBanned", this.user.getIsBanned());            "isBanned", this.user.getIsBanned() != null && this.user.getIsBanned(),

            userInfo.put("isSuperAdmin", this.user.getIsSuperAdmin());            "message", "Account found. " + (this.user.getIsAdmin() != null && this.user.getIsAdmin() ? "Already admin." : "Use super-admin-setup to become admin.")

            userInfo.put("status", this.user.getStatus());        ));

                }

            // Check if user is online

            boolean isOnline = this.sessionService.hasActiveSession(user.getUsername());    // Special endpoint to set specific email as super admin (use once then remove)

            int sessionCount = this.sessionService.getSessionCount(user.getUsername());    @PostMapping("/set-super-admin")

            userInfo.put("isOnline", isOnline);    public ResponseEntity<?> setSuperAdmin(@RequestBody Map<String, String> request) {

            userInfo.put("sessionCount", this.sessionCount);        String email = request.get("email");

                    String secretKey = request.get("secret");

            return userInfo;        

        }).collect(Collectors.toList());        System.out.println("🔐 Super Admin Setup Request - Email: " + email);

                

        // Separate online and offline users        // Check secret key from environment variable

        Map<String, List<Map<String, Object>>> result = new HashMap<>();        if (!this.adminSetupSecret.equals(secretKey)) {

        this.result.put("online", userList.stream().filter(user -> (Boolean) user.get("isOnline")).collect(Collectors.toList()));            System.out.println("❌ Invalid secret key provided");

        this.result.put("offline", userList.stream().filter(user -> !(Boolean) user.get("isOnline")).collect(Collectors.toList()));            return ResponseEntity.status(403).body(Map.of("error", "Invalid secret key"));

        this.result.put("total", userList);        }

                

        return ResponseEntity.ok(result);        if (email == null || email.isEmpty()) {

    }            System.out.println("❌ Email is empty");

                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));

    /**        }

     * Promote user to admin        

     */        User user = this.userRepository.findByEmail(email.toLowerCase().trim());

    @PostMapping("/promote/{userId}")        if (user == null) {

    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId, HttpSession session) {            System.out.println("❌ User not found with email: " + email);

        if (!this.isSuperAdmin(session)) {            return ResponseEntity.status(404).body(Map.of("error", "User with email " + email + " not found. Please register first."));

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Super admin access required");        }

        }        

                System.out.println("✅ Found user: " + user.getUsername() + " (email: " + user.getEmail() + ")");

        User user = this.userRepository.findById(userId).orElse(null);        System.out.println("📝 Setting isAdmin=true, isBanned=false");

        if (user == null) {        

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");        user.setIsAdmin(true);

        }        user.setIsBanned(false);

                User savedUser = this.userRepository.save(user);

        user.setIsAdmin(true);        

        userRepository.save(user);        System.out.println("💾 Saved to database - Admin status: " + savedUser.getIsAdmin());

                

        log.info("User {} promoted to admin by super admin {}", user.getUsername(), session.getAttribute("username"));        return ResponseEntity.ok(Map.of(

        return ResponseEntity.ok("User " + user.getUsername() + " promoted to admin");            "message", "User " + user.getUsername() + " is now Super Admin!",

    }            "email", email,

                "username", user.getUsername(),

    /**            "isAdmin", true

     * Demote admin to regular user        ));

     */    }

    @PostMapping("/demote/{userId}")

    public ResponseEntity<?> demoteAdmin(@PathVariable Long userId, HttpSession session) {    // Initialize first admin if no admins exist (one-time setup)

        if (!isSuperAdmin(session)) {    @PostMapping("/initialize-first-admin")

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Super admin access required");    public ResponseEntity<?> initializeFirstAdmin(HttpSession session) {

        }        String username = (String) session.getAttribute("username");

                if (username == null) {

        User user = this.userRepository.findById(userId).orElse(null);            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated. Please login first."));

        if (user == null) {        }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        }        // Check if any admins already exist

                List<User> allUsers = this.userRepository.findAll();

        if (user.getIsSuperAdmin()) {        boolean adminExists = this.allUsers.stream()

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot demote super admin");            .anyMatch(u -> u.getIsAdmin() != null && u.getIsAdmin());

        }        

                if (adminExists) {

        user.setIsAdmin(false);            return ResponseEntity.status(403).body(Map.of("error", "Admin already exists. Contact existing admin for privileges."));

        this.userRepository.save(user);        }

        

        log.info("Admin {} demoted to user by super admin {}", user.getUsername(), session.getAttribute("username"));        // No admins exist, make current user the first admin

        return ResponseEntity.ok("User " + user.getUsername() + " demoted to regular user");        User currentUser = this.userRepository.findByUsername(this.username);

    }        if (currentUser == null) {

                return ResponseEntity.status(404).body(Map.of("error", "User not found"));

    /**        }

     * Mute user

     */        currentUser.setIsAdmin(true);

    @PostMapping("/mute/{userId}")        currentUser.setIsBanned(false); // Ensure not banned

    public ResponseEntity<?> muteUser(@PathVariable Long userId, HttpSession session) {        this.userRepository.save(this.currentUser);

        if (!isAdmin(session)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");        return ResponseEntity.ok(Map.of(

        }            "message", "You are now the first admin! Welcome, Super Admin!",

                    "username", this.username,

        User this.user = this.userRepository.findById(userId).orElse(null);            "isAdmin", true

        if (this.user == null) {        ));

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");    }

        }

            // Check if current user is admin

        if (user.getIsSuperAdmin()) {    @GetMapping("/check")

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot mute super admin");    public ResponseEntity<?> checkAdmin(HttpSession session) {

        }        String username = (String) session.getAttribute("username");

                System.out.println("🔍 Admin Check - Session username: " + username);

        user.setIsMuted(!user.getIsMuted());        

        userRepository.save(user);        if (username == null) {

                    System.out.println("❌ Not authenticated - no username in session");

        String action = user.getIsMuted() ? "muted" : "unmuted";            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        log.info("User {} {} by admin {}", user.getUsername(), action, session.getAttribute("username"));        }

        return ResponseEntity.ok("User " + user.getUsername() + " " + action);

    }        User user = this.userRepository.findByUsername(this.username);

            if (user == null) {

    /**            System.out.println("❌ User not found in database: " + username);

     * Ban/Unban user            return ResponseEntity.status(404).body(Map.of("error", "User not found"));

     */        }

    @PostMapping("/ban/{userId}")

    public ResponseEntity<?> banUser(@PathVariable Long userId, HttpSession session) {        boolean isAdmin = this.user.getIsAdmin() != null && this.user.getIsAdmin();

        if (!isAdmin(session)) {        System.out.println("📊 User: " + this.username + " | Email: " + this.user.getEmail() + " | isAdmin: " + isAdmin);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");        

        }        return ResponseEntity.ok(Map.of(

                    "isAdmin", isAdmin,

        User this.user = this.userRepository.findById(userId).orElse(null);            "username", username,

        if (this.user == null) {            "email", user.getEmail()

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");        ));

        }    }

        

        if (user.getIsSuperAdmin()) {    // Get all users (admin only)

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot ban super admin");    @GetMapping("/users")

        }    public ResponseEntity<?> getAllUsers(HttpSession session) {

                String username = (String) session.getAttribute("username");

        user.setIsBanned(!user.getIsBanned());        System.out.println(" [getAllUsers] Session username: " + username);

        userRepository.save(user);        if (username == null) {

                    return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        // If user is banned, invalidate all their sessions        }

        if (user.getIsBanned()) {

            Set<String> userSessions = sessionService.getAllSessionIds(user.getUsername());        User admin = this.userRepository.findByUsername(username);

            log.info("Banning user {} and invalidating {} sessions", user.getUsername(), userSessions.size());        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {

            // Note: We can't directly invalidate HttpSessions here, but we remove them from tracking            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));

            sessionService.removeUserSession(user.getUsername());        }

        }

                // Get all connected usernames (includes guests)

        String action = user.getIsBanned() ? "banned" : "unbanned";        var connectedUsernames = this.userTrackingService.getConnectedUsernames();

        log.info("User {} {} by admin {}", user.getUsername(), action, session.getAttribute("username"));        

        return ResponseEntity.ok("User " + user.getUsername() + " " + action);        List<User> users = this.userRepository.findAll();

    }        List<Map<String, Object>> userList = users.stream().map(user -> {

                Map<String, Object> userInfo = new HashMap<>();

    /**            userInfo.put("id", user.getId());

     * Delete user (super admin only)            userInfo.put("username", user.getUsername());

     */            userInfo.put("email", user.getEmail());

    @DeleteMapping("/delete/{userId}")            userInfo.put("isAdmin", user.getIsAdmin() != null && user.getIsAdmin());

    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpSession session) {            userInfo.put("isBanned", this.user.getIsBanned() != null && this.user.getIsBanned());

        if (!this.isSuperAdmin(session)) {            userInfo.put("banReason", this.user.getBanReason());

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Super admin access required");            userInfo.put("gender", this.user.getGender());

        }            userInfo.put("age", this.user.getAge());

                    userInfo.put("online", connectedUsernames.contains(this.user.getUsername()));

        User user = this.userRepository.findById(userId).orElse(null);            userInfo.put("isGuest", false);

        if (user == null) {            return userInfo;

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");        }).collect(Collectors.toList());

        }        

                // Add guest users (connected but not registered)

        if (user.getIsSuperAdmin()) {        List<String> registeredUsernames = users.stream()

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot delete super admin");            .map(User::getUsername)

        }            .collect(Collectors.toList());

                    

        // Remove all user sessions        connectedUsernames.stream()

        sessionService.removeUserSession(user.getUsername());            .filter(guestUsername -> !registeredUsernames.contains(guestUsername))

                    .forEach(guestUsername -> {

        // Delete user                Map<String, Object> guestInfo = new HashMap<>();

        String deletedUsername = user.getUsername();                guestInfo.put("id", null);

        this.userRepository.delete(user);                guestInfo.put("username", guestUsername);

                        guestInfo.put("email", "");

        log.info("User {} deleted by super admin {}", deletedUsername, session.getAttribute("username"));                guestInfo.put("isAdmin", false);

        return ResponseEntity.ok("User " + deletedUsername + " deleted successfully");                guestInfo.put("isBanned", false);

    }                guestInfo.put("banReason", null);

                    guestInfo.put("gender", null);

    /**                guestInfo.put("age", null);

     * Clear user's chat history (placeholder - would need chat message entity)                guestInfo.put("online", true);

     */                guestInfo.put("isGuest", true);

    @PostMapping("/clear-chat/{userId}")                userList.add(guestInfo);

    public ResponseEntity<?> clearUserChat(@PathVariable Long userId, HttpSession session) {            });

        if (!isAdmin(session)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");        return ResponseEntity.ok(Map.of("users", userList, "total", userList.size()));

        }    }

        

        User user = this.userRepository.findById(userId).orElse(null);    // Ban a user

        if (user == null) {    @PostMapping("/ban")

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");    public ResponseEntity<?> banUser(@RequestBody Map<String, String> request, HttpSession session) {

        }        String username = (String) session.getAttribute("username");

                if (username == null) {

        // TODO: Implement chat message deletion when chat history is stored in database            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        // For now, just log the action        }

        log.info("Chat history cleared for user {} by admin {}", user.getUsername(), session.getAttribute("username"));

        return ResponseEntity.ok("Chat history cleared for user " + user.getUsername());        User admin = this.userRepository.findByUsername(this.username);

    }        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {

                return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));

    /**        }

     * Get admin dashboard stats

     */        String targetUsername = request.get("username");

    @GetMapping("/stats")        String reason = request.getOrDefault("reason", "Violated community guidelines");

    public ResponseEntity<?> getAdminStats(HttpSession session) {

        if (!isAdmin(session)) {        if (targetUsername == null || targetUsername.isEmpty()) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));

        }        }

        

        long totalUsers = this.userRepository.count();        User targetUser = this.userRepository.findByUsername(targetUsername);

        long adminUsers = this.userRepository.countByIsAdminTrue();        if (targetUser == null) {

        long mutedUsers = this.userRepository.countByIsMutedTrue();            return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        long bannedUsers = this.userRepository.countByIsBannedTrue();        }

        

        // Count online users        // Prevent banning other admins

        List<User> allUsers = this.userRepository.findAll();        if (targetUser.getIsAdmin() != null && targetUser.getIsAdmin()) {

        long onlineUsers = allUsers.stream().mapToLong(user -> sessionService.hasActiveSession(user.getUsername()) ? 1 : 0).sum();            return ResponseEntity.status(403).body(Map.of("error", "Cannot ban another admin"));

                }

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", totalUsers);        targetUser.setIsBanned(true);

        stats.put("onlineUsers", onlineUsers);        targetUser.setBanReason(reason);

        stats.put("offlineUsers", totalUsers - onlineUsers);        this.userRepository.save(targetUser);

        stats.put("adminUsers", adminUsers);

        stats.put("mutedUsers", mutedUsers);        return ResponseEntity.ok(Map.of(

        stats.put("bannedUsers", bannedUsers);            "message", "User banned successfully",

                    "username", targetUsername,

        return ResponseEntity.ok(stats);            "reason", reason

    }        ));

}    }

    // Unban a user
    @PostMapping("/unban")
    public ResponseEntity<?> unbanUser(@RequestBody Map<String, String> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        String targetUsername = request.get("username");
        if (targetUsername == null || targetUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        User targetUser = this.userRepository.findByUsername(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        targetUser.setIsBanned(false);
        targetUser.setBanReason(null);
        this.userRepository.save(targetUser);

        return ResponseEntity.ok(Map.of(
            "message", "User unbanned successfully",
            "username", targetUsername
        ));
    }

    // Make user admin
    @PostMapping("/make-admin")
    public ResponseEntity<?> makeAdmin(@RequestBody Map<String, String> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        String targetUsername = request.get("username");
        if (targetUsername == null || targetUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        User targetUser = this.userRepository.findByUsername(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        targetUser.setIsAdmin(true);
        this.userRepository.save(targetUser);

        return ResponseEntity.ok(Map.of(
            "message", "User granted admin privileges",
            "username", targetUsername
        ));
    }

    // Remove admin privileges
    @PostMapping("/remove-admin")
    public ResponseEntity<?> removeAdmin(@RequestBody Map<String, String> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        String targetUsername = request.get("username");
        if (targetUsername == null || targetUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        // Prevent removing own admin status
        if (targetUsername.equalsIgnoreCase(username)) {
            return ResponseEntity.status(403).body(Map.of("error", "Cannot remove your own admin privileges"));
        }

        User targetUser = this.userRepository.findByUsername(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        targetUser.setIsAdmin(false);
        this.userRepository.save(targetUser);

        return ResponseEntity.ok(Map.of(
            "message", "Admin privileges removed",
            "username", targetUsername
        ));
    }

    // Delete user account (admin only)
    @PostMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestBody Map<String, String> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        String targetUsername = request.get("username");
        if (targetUsername == null || targetUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        // Prevent deleting own account
        if (targetUsername.equalsIgnoreCase(username)) {
            return ResponseEntity.status(403).body(Map.of("error", "Cannot delete your own account"));
        }

        User targetUser = this.userRepository.findByUsername(targetUsername);
        if (targetUser == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // Prevent deleting other admins
        if (targetUser.getIsAdmin() != null && targetUser.getIsAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Cannot delete another admin"));
        }

        this.userRepository.delete(targetUser);

        return ResponseEntity.ok(Map.of(
            "message", "User deleted successfully",
            "username", targetUsername
        ));
    }
    
    // Kick user (disconnect from chat) - works for both guests and registered users
    @PostMapping("/kick-user")
    public ResponseEntity<?> kickUser(@RequestBody Map<String, String> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || !Boolean.TRUE.equals(admin.getIsAdmin())) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        String targetUsername = request.get("username");
        if (targetUsername == null || targetUsername.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        // Prevent kicking yourself
        if (targetUsername.equalsIgnoreCase(username)) {
            return ResponseEntity.status(403).body(Map.of("error", "Cannot kick yourself"));
        }

        // Check if target is admin
        User targetUser = this.userRepository.findByUsername(targetUsername);
        if (targetUser != null && targetUser.getIsAdmin() != null && targetUser.getIsAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Cannot kick another admin"));
        }

        // Remove from tracking service
        this.userTrackingService.removeUser(targetUsername);

        // Send kick notification to the specific user
        Map<String, Object> kickMessage = new HashMap<>();
        kickMessage.put("type", "KICK");
        kickMessage.put("username", targetUsername);
        kickMessage.put("message", "You have been kicked by an admin");
        
        this.messagingTemplate.convertAndSend("/topic/kick/" + targetUsername, kickMessage);

        return ResponseEntity.ok(Map.of(
            "message", "User kicked successfully",
            "username", targetUsername
        ));
    }
    
    // Clear all chat messages
    @PostMapping("/clear-chat")
    public ResponseEntity<?> clearChat(HttpSession session) {
        String username = (String) session.getAttribute("username");
        System.out.println("🧹 Clear Chat Request - Admin: " + username);
        
        if (username == null) {
            System.out.println("❌ Not authenticated");
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || admin.getIsAdmin() == null || !admin.getIsAdmin()) {
            System.out.println("❌ Not an admin: " + username);
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        }

        try {
            long messageCount = this.messageRepository.count();
            this.messageRepository.deleteAll();
            
            // Notify all users that chat has been cleared
            Map<String, Object> clearNotification = new HashMap<>();
            clearNotification.put("type", "CLEAR_CHAT");
            clearNotification.put("message", "Chat has been cleared by an admin");
            clearNotification.put("admin", username);
            
            this.messagingTemplate.convertAndSend("/topic/messages", clearNotification);
            
            System.out.println("✅ Cleared " + messageCount + " messages by admin: " + username);
            
            return ResponseEntity.ok(Map.of(
                "message", "Chat cleared successfully",
                "deletedCount", messageCount
            ));
        } catch (RuntimeException e) {
            System.err.println("❌ Error clearing chat: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to clear chat"));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error clearing chat: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to clear chat"));
        }
    }
    // Delete specific message by ID
    @DeleteMapping("/delete-message")
    public ResponseEntity<?> deleteMessage(@RequestParam Long messageId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        System.out.println(" Delete Message Request - Admin: " + username + ", MessageID: " + messageId);

        if (username == null) {
            System.out.println(" Not authenticated");
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        User admin = this.userRepository.findByUsername(username);
        if (admin == null || admin.getIsAdmin() == null || !admin.getIsAdmin()) {
            System.out.println(" Not an admin: " + username);
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        }

        try {
            if (!this.messageRepository.existsById(messageId)) {
                return ResponseEntity.status(404).body(Map.of("error", "Message not found"));
            }

            this.messageRepository.deleteById(messageId);

            // Notify all users that a message was deleted
            Map<String, Object> deleteNotification = new HashMap<>();
            deleteNotification.put("type", "DELETE_MESSAGE");
            deleteNotification.put("messageId", messageId);
            deleteNotification.put("admin", username);

            this.messagingTemplate.convertAndSend("/topic/messages", deleteNotification);

            System.out.println(" Deleted message " + messageId + " by admin: " + username);

            return ResponseEntity.ok(Map.of(
                "message", "Message deleted successfully",
                "messageId", messageId
            ));
        } catch (RuntimeException e) {
            System.err.println("❌ Error deleting message: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete message"));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error deleting message: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete message"));
        }
    }

    // Fix username mismatch - temporary endpoint
    @PostMapping("/fix-username")
    public ResponseEntity<?> fixUsername(HttpSession session) {
        String sessionUsername = (String) session.getAttribute("username");
        System.out.println("🔧 [FIX-USERNAME] Session username: " + sessionUsername);

        if (sessionUsername == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        try {
            // Find user by email that has is_admin = true
            User adminUser = this.userRepository.findByEmail("gokulkannans92@gmail.com");
            
            if (adminUser == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Admin user not found with that email"));
            }

            String oldUsername = adminUser.getUsername();
            System.out.println("🔧 [FIX-USERNAME] Found user - Old username: " + oldUsername + ", Email: " + adminUser.getEmail());
            
            // Update username to match session
            adminUser.setUsername(sessionUsername);
            this.userRepository.save(adminUser);

            System.out.println("✅ [FIX-USERNAME] Updated username from '" + oldUsername + "' to '" + sessionUsername + "'");

            return ResponseEntity.ok(Map.of(
                "message", "Username fixed successfully",
                "oldUsername", oldUsername,
                "newUsername", sessionUsername,
                "email", adminUser.getEmail(),
                "isAdmin", adminUser.getIsAdmin()
            ));
        } catch (Exception e) {
            System.err.println("❌ [FIX-USERNAME] Error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fix username: " + e.getMessage()));
        }
    }

}
