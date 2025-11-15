package com.example.demo.service;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.enums.UserRole;
import com.example.demo.model.User;
import com.example.demo.enums.Permission;
import com.example.demo.enums.PermissionCategory;
import com.example.demo.repository.UserRepository;

/**
 * Permission validation and enforcement service.
 * Provides centralized permission checking for all system operations.
 */
@Service
@Transactional(readOnly = true)
public class PermissionService {
        /**
         * Check if user can send links in messages (by User object)
         */
        public boolean canSendLink(User user) {
            if (user == null) return false;
            if (user.getIsBanned() || user.getIsMuted()) {
                return false;
            }
            return user.hasPermission(Permission.SEND_LINKS);
        }
    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private UserRepository userRepository;

    // === CHAT PERMISSIONS ===

    /**
     * Check if user can send messages
     */
    public boolean canSendMessage(String username) {
        try {
            User user = findUserByUsername(username);

            // Check if user is banned or muted
            if (user.getIsBanned()) {
                log.debug("User {} is banned - cannot send messages", username);
                return false;
            }

            if (user.getIsMuted()) {
                log.debug("User {} is muted - cannot send messages", username);
                return false;
            }

            // Check role-based permission
            boolean hasPermission = user.hasPermission(Permission.SEND_MESSAGES);
            log.debug("User {} can send messages: {}", username, hasPermission);
            return hasPermission;

        } catch (Exception e) {
            log.warn("Error checking send message permission for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can send links in messages
     */
    public boolean canSendLinks(String username) {
        try {
            User user = findUserByUsername(username);

            // Check if user is banned or muted
            if (user.getIsBanned() || user.getIsMuted()) {
                return false;
            }

            return user.hasPermission(Permission.SEND_LINKS);

        } catch (Exception e) {
            log.warn("Error checking send links permission for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can upload images
     */
    public boolean canUploadImages(String username) {
        try {
            User user = findUserByUsername(username);

            // Check if user is banned or muted
            if (user.getIsBanned() || user.getIsMuted()) {
                return false;
            }

            return user.hasPermission(Permission.UPLOAD_IMAGES);

        } catch (Exception e) {
            log.warn("Error checking upload images permission for {}: {}", username, e.getMessage());
            return false;
        }
    }

    // === MODERATION PERMISSIONS ===

    /**
     * Check if user can delete messages
     */
    public boolean canDeleteMessage(String moderatorUsername, String messageAuthorUsername) {
        try {
            User moderator = findUserByUsername(moderatorUsername);
            User messageAuthor = findUserByUsername(messageAuthorUsername);

            // Users can always delete their own messages
            if (moderator.getUsername().equals(messageAuthor.getUsername())) {
                return moderator.hasPermission(Permission.DELETE_OWN_MESSAGES);
            }

            // Check if moderator can delete others' messages
            return moderator.hasPermission(Permission.DELETE_MESSAGES);

        } catch (Exception e) {
            log.warn("Error checking delete message permission for {} deleting message by {}: {}",
                     moderatorUsername, messageAuthorUsername, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can mute another user
     */
    public boolean canMuteUser(String moderatorUsername, String targetUsername) {
        try {
            User moderator = findUserByUsername(moderatorUsername);
            User target = findUserByUsername(targetUsername);

            // Check basic permission
            if (!moderator.hasPermission(Permission.MUTE_USERS)) {
                return false;
            }

            // Users cannot mute themselves
            if (moderator.getUsername().equals(target.getUsername())) {
                return false;
            }

            // Cannot mute users with equal or higher role
            if (target.getUserRole().isHigherThanOrEqual(moderator.getUserRole())) {
                log.debug("User {} cannot mute {} - target has equal or higher role",
                         moderatorUsername, targetUsername);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.warn("Error checking mute permission for {} muting {}: {}",
                     moderatorUsername, targetUsername, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can ban another user
     */
    public boolean canBanUser(String adminUsername, String targetUsername) {
        try {
            User admin = findUserByUsername(adminUsername);
            User target = findUserByUsername(targetUsername);

            // Check basic permission
            if (!admin.hasPermission(Permission.BAN_USERS)) {
                return false;
            }

            // Users cannot ban themselves
            if (admin.getUsername().equals(target.getUsername())) {
                return false;
            }

            // Cannot ban users with equal or higher role
            if (target.getUserRole().isHigherThanOrEqual(admin.getUserRole())) {
                log.debug("User {} cannot ban {} - target has equal or higher role",
                         adminUsername, targetUsername);
                return false;
            }

            return true;

        } catch (Exception e) {
            log.warn("Error checking ban permission for {} banning {}: {}",
                     adminUsername, targetUsername, e.getMessage());
            return false;
        }
    }

    // === ADMINISTRATION PERMISSIONS ===

    /**
     * Check if user can assign roles to target role
     */
    public boolean canAssignRole(String assignerUsername, UserRole targetRole) {
        try {
            User assigner = findUserByUsername(assignerUsername);

            // Check basic permission
            if (!assigner.hasPermission(Permission.MANAGE_ROLES)) {
                return false;
            }

            // Check if assigner can promote to target role
            return assigner.getUserRole().canPromoteTo(targetRole);

        } catch (Exception e) {
            log.warn("Error checking role assignment permission for {} assigning {}: {}",
                     assignerUsername, targetRole, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can access admin panel
     */
    public boolean canAccessAdminPanel(String username) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(Permission.ACCESS_ADMIN_PANEL);

        } catch (Exception e) {
            log.warn("Error checking admin panel access for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can view audit logs
     */
    public boolean canViewAuditLogs(String username) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(Permission.VIEW_AUDIT_LOG);

        } catch (Exception e) {
            log.warn("Error checking audit log access for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can view system statistics
     */
    public boolean canViewSystemStats(String username) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(Permission.VIEW_SYSTEM_STATS);

        } catch (Exception e) {
            log.warn("Error checking system stats access for {}: {}", username, e.getMessage());
            return false;
        }
    }

    // === SYSTEM PERMISSIONS ===

    /**
     * Check if user can perform system configuration
     */
    public boolean canConfigureSystem(String username) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(Permission.SYSTEM_CONFIGURATION);

        } catch (Exception e) {
            log.warn("Error checking system configuration permission for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can access database
     */
    public boolean canAccessDatabase(String username) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(Permission.DATABASE_ACCESS);

        } catch (Exception e) {
            log.warn("Error checking database access permission for {}: {}", username, e.getMessage());
            return false;
        }
    }

    // === UTILITY METHODS ===

    /**
     * Check if user has a specific permission
     */
    public boolean hasPermission(String username, Permission permission) {
        try {
            User user = findUserByUsername(username);
            return user.hasPermission(permission);

        } catch (Exception e) {
            log.warn("Error checking permission {} for {}: {}", permission, username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user has a specific permission by name
     */
    public boolean hasPermission(String username, String permissionName) {
        try {
            Permission permission = Permission.fromName(permissionName);
            if (permission == null) {
                log.warn("Unknown permission: {}", permissionName);
                return false;
            }

            return hasPermission(username, permission);

        } catch (Exception e) {
            log.warn("Error checking permission {} for {}: {}", permissionName, username, e.getMessage());
            return false;
        }
    }

    /**
     * Get all permissions for a user
     */
    public Set<Permission> getUserPermissions(String username) {
        try {
            User user = findUserByUsername(username);
            return Set.copyOf(user.getUserRole().getPermissions());

        } catch (Exception e) {
            log.warn("Error getting permissions for {}: {}", username, e.getMessage());
            return Set.of();
        }
    }

    /**
     * Check if user has any permissions in a specific category
     */
    public boolean hasPermissionsInCategory(String username, PermissionCategory category) {
        try {
            User user = findUserByUsername(username);
            return user.getUserRole().getPermissions().stream()
                    .anyMatch(p -> p.getCategory() == category);

        } catch (Exception e) {
            log.warn("Error checking category permissions for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Get user's role information
     */
    public UserRole getUserRole(String username) {
        try {
            User user = findUserByUsername(username);
            return user.getUserRole();

        } catch (Exception e) {
            log.warn("Error getting role for {}: {}", username, e.getMessage());
            return UserRole.NEW_MEMBER; // Default role
        }
    }

    /**
     * Check if user is a moderator or higher
     */
    public boolean isModeratorOrHigher(String username) {
        try {
            User user = findUserByUsername(username);
            return user.getUserRole().getLevel() >= UserRole.MODERATOR.getLevel();

        } catch (Exception e) {
            log.warn("Error checking moderator status for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user is an admin or higher
     */
    public boolean isAdminOrHigher(String username) {
        try {
            User user = findUserByUsername(username);
            return user.getUserRole().getLevel() >= UserRole.ADMIN.getLevel();

        } catch (Exception e) {
            log.warn("Error checking admin status for {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user is a super admin
     */
    public boolean isSuperAdmin(String username) {
        try {
            User user = findUserByUsername(username);
            return user.getUserRole() == UserRole.SUPER_ADMIN;

        } catch (Exception e) {
            log.warn("Error checking super admin status for {}: {}", username, e.getMessage());
            return false;
        }
    }

    // === PRIVATE HELPER METHODS ===

    private User findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found: {}", username);
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    /**
     * Validate message content based on user permissions
     */
    public MessageValidationResult validateMessageContent(String username, String content) {
        try {
            User user = findUserByUsername(username);

            // Check if user is banned or muted
            if (user.getIsBanned()) {
                return MessageValidationResult.rejected("You are banned from sending messages");
            }

            if (user.getIsMuted()) {
                return MessageValidationResult.rejected("You are muted and cannot send messages");
            }

            // Check basic send permission
            if (!user.hasPermission(Permission.SEND_MESSAGES)) {
                return MessageValidationResult.rejected("You don't have permission to send messages");
            }

            // Check for links if user doesn't have permission
            if (containsLinks(content) && !user.hasPermission(Permission.SEND_LINKS)) {
                return MessageValidationResult.rejected("You don't have permission to send links");
            }

            return MessageValidationResult.approved();

        } catch (Exception e) {
            log.warn("Error validating message content for {}: {}", username, e.getMessage());
            return MessageValidationResult.rejected("Validation error occurred");
        }
    }

    private boolean containsLinks(String content) {
        if (content == null) {
            return false;
        }
        // Simple URL detection regex
        return content.matches(".*https?://.*") || content.matches(".*www\\..*");
    }

    /**
     * Result class for message validation
     */
    public static class MessageValidationResult {
        private final boolean approved;
        private final String rejectionReason;

        private MessageValidationResult(boolean approved, String rejectionReason) {
            this.approved = approved;
            this.rejectionReason = rejectionReason;
        }

        public static MessageValidationResult approved() {
            return new MessageValidationResult(true, null);
        }

        public static MessageValidationResult rejected(String reason) {
            return new MessageValidationResult(false, reason);
        }

        public boolean isApproved() {
            return approved;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }
    }
}
