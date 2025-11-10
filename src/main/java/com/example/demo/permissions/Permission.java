package com.example.demo.permissions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Granular permissions system for the community chat
 * Each permission represents a specific action that can be performed
 */
public enum Permission {

    // === CHAT PERMISSIONS ===

    SEND_MESSAGES("SEND_MESSAGES", "Send messages in chat", PermissionCategory.CHAT, 1),
    SEND_LINKS("SEND_LINKS", "Send links in messages", PermissionCategory.CHAT, 2),
    UPLOAD_IMAGES("UPLOAD_IMAGES", "Upload image files", PermissionCategory.CHAT, 3),
    USE_CUSTOM_EMOJIS("USE_CUSTOM_EMOJIS", "Use custom emoji reactions", PermissionCategory.CHAT, 4),
    ACCESS_VIP_CHANNELS("ACCESS_VIP_CHANNELS", "Access VIP-only chat channels", PermissionCategory.CHAT, 5),
    DELETE_OWN_MESSAGES("DELETE_OWN_MESSAGES", "Delete your own messages", PermissionCategory.CHAT, 1),

    // === MODERATION PERMISSIONS ===

    DELETE_MESSAGES("DELETE_MESSAGES", "Delete any user's messages", PermissionCategory.MODERATION, 10),
    MUTE_USERS("MUTE_USERS", "Temporarily mute users", PermissionCategory.MODERATION, 11),
    BAN_USERS("BAN_USERS", "Permanently ban users", PermissionCategory.MODERATION, 12),
    VIEW_AUDIT_LOG("VIEW_AUDIT_LOG", "View moderation and system audit logs", PermissionCategory.MODERATION, 13),
    WARN_USERS("WARN_USERS", "Issue warnings to users", PermissionCategory.MODERATION, 14),
    QUARANTINE_MESSAGES("QUARANTINE_MESSAGES", "Quarantine inappropriate messages", PermissionCategory.MODERATION, 15),

    // === ADMINISTRATION PERMISSIONS ===

    MANAGE_ROLES("MANAGE_ROLES", "Assign and manage user roles", PermissionCategory.ADMINISTRATION, 20),
    ACCESS_ADMIN_PANEL("ACCESS_ADMIN_PANEL", "Access administrative dashboard", PermissionCategory.ADMINISTRATION, 21),
    VIEW_SYSTEM_STATS("VIEW_SYSTEM_STATS", "View system statistics and analytics", PermissionCategory.ADMINISTRATION, 22),
    MANAGE_ANNOUNCEMENTS("MANAGE_ANNOUNCEMENTS", "Create and manage system announcements", PermissionCategory.ADMINISTRATION, 23),
    VIEW_USER_REPORTS("VIEW_USER_REPORTS", "View user-submitted reports", PermissionCategory.ADMINISTRATION, 24),
    MANAGE_BLACKLIST("MANAGE_BLACKLIST", "Manage IP and word blacklist", PermissionCategory.ADMINISTRATION, 25),
    EXPORT_CHAT_DATA("EXPORT_CHAT_DATA", "Export chat history and data", PermissionCategory.ADMINISTRATION, 26),

    // === SYSTEM PERMISSIONS ===

    SYSTEM_CONFIGURATION("SYSTEM_CONFIGURATION", "Modify system configuration", PermissionCategory.SYSTEM, 30),
    DATABASE_ACCESS("DATABASE_ACCESS", "Direct database access and modifications", PermissionCategory.SYSTEM, 31),
    MANAGE_PERMISSIONS("MANAGE_PERMISSIONS", "Modify system permissions structure", PermissionCategory.SYSTEM, 32),
    OWNERSHIP_TRANSFER("OWNERSHIP_TRANSFER", "Transfer system ownership", PermissionCategory.SYSTEM, 33),
    SYSTEM_BACKUP("SYSTEM_BACKUP", "Create and restore system backups", PermissionCategory.SYSTEM, 34),
    VIEW_SENSITIVE_DATA("VIEW_SENSITIVE_DATA", "Access sensitive user and system data", PermissionCategory.SYSTEM, 35);

    private final String name;
    private final String description;
    private final PermissionCategory category;
    private final int level;

    Permission(String name, String description, PermissionCategory category, int level) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public PermissionCategory getCategory() {
        return category;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Permission categories for organization and UI display
     */
    public enum PermissionCategory {
        CHAT("Chat Permissions", "Basic chat functionality"),
        MODERATION("Moderation", "Content and user moderation"),
        ADMINISTRATION("Administration", "Community and user management"),
        SYSTEM("System", "System-level and critical operations");

        private final String displayName;
        private final String description;

        PermissionCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Get all permissions in a specific category
     */
    public static List<Permission> getPermissionsByCategory(PermissionCategory category) {
        return Arrays.stream(values())
                .filter(permission -> permission.category == category)
                .sorted((p1, p2) -> Integer.compare(p1.level, p2.level))
                .collect(Collectors.toList());
    }

    /**
     * Get all permission categories
     */
    public static List<PermissionCategory> getCategories() {
        return Arrays.asList(PermissionCategory.values());
    }

    /**
     * Find permission by name
     */
    public static Permission fromName(String permissionName) {
        if (permissionName == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(permission -> permission.name.equals(permissionName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get basic chat permissions (available to all users)
     */
    public static Set<Permission> getBasicChatPermissions() {
        return Arrays.stream(values())
                .filter(p -> p.category == PermissionCategory.CHAT && p.level <= 5)
                .collect(Collectors.toSet());
    }

    /**
     * Get moderation permissions
     */
    public static Set<Permission> getModerationPermissions() {
        return Arrays.stream(values())
                .filter(p -> p.category == PermissionCategory.MODERATION)
                .collect(Collectors.toSet());
    }

    /**
     * Get administrative permissions
     */
    public static Set<Permission> getAdministrativePermissions() {
        return Arrays.stream(values())
                .filter(p -> p.category == PermissionCategory.ADMINISTRATION)
                .collect(Collectors.toSet());
    }

    /**
     * Get system permissions (highest level)
     */
    public static Set<Permission> getSystemPermissions() {
        return Arrays.stream(values())
                .filter(p -> p.category == PermissionCategory.SYSTEM)
                .collect(Collectors.toSet());
    }

    /**
     * Check if this permission requires special logging/auditing
     */
    public boolean requiresAuditLogging() {
        return this.category == PermissionCategory.MODERATION ||
               this.category == PermissionCategory.ADMINISTRATION ||
               this.category == PermissionCategory.SYSTEM;
    }

    /**
     * Check if this permission is considered critical and requires additional verification
     */
    public boolean isCritical() {
        return this == BAN_USERS ||
               this == SYSTEM_CONFIGURATION ||
               this == DATABASE_ACCESS ||
               this == OWNERSHIP_TRANSFER ||
               this == MANAGE_PERMISSIONS;
    }

    /**
     * Get display name with proper formatting
     */
    public String getDisplayName() {
        return name.replace("_", " ")
                  .toLowerCase()
                  .replaceAll("\\b\\w", b -> b.toUpperCase());
    }

    /**
     * Check if this permission can be assigned to a specific role level
     */
    public boolean isAssignableToRoleLevel(int roleLevel) {
        switch (this.category) {
            case CHAT:
                return true; // Chat permissions can be assigned to any role
            case MODERATION:
                return roleLevel >= 4; // Moderator and above
            case ADMINISTRATION:
                return roleLevel >= 5; // Admin and above
            case SYSTEM:
                return roleLevel >= 6; // Super Admin only
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", name, category.getDisplayName(), description);
    }
}