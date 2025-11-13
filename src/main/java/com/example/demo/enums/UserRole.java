package com.example.demo.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Comprehensive user role system with six-tier hierarchy
 * Each role has specific permissions and display properties
 */
public enum UserRole {
    NEW_MEMBER("New Member", 1, "#94a3b8", "👤",
        "New community member with limited privileges",
        Arrays.asList(Permission.SEND_MESSAGES)),

    MEMBER("Member", 2, "#06b6d4", "👥",
        "Regular community member with standard privileges",
        Arrays.asList(
            Permission.SEND_MESSAGES,
            Permission.SEND_LINKS,
            Permission.UPLOAD_IMAGES
        )),

    VIP("VIP", 3, "#8b5cf6", "⭐",
        "Valued community member with enhanced privileges",
        Arrays.asList(
            Permission.SEND_MESSAGES,
            Permission.SEND_LINKS,
            Permission.UPLOAD_IMAGES,
            Permission.USE_CUSTOM_EMOJIS,
            Permission.ACCESS_VIP_CHANNELS
        )),

    MODERATOR("Moderator", 4, "#f59e0b", "🛡️",
        "Community moderator with moderation privileges",
        Arrays.asList(
            Permission.SEND_MESSAGES,
            Permission.SEND_LINKS,
            Permission.UPLOAD_IMAGES,
            Permission.USE_CUSTOM_EMOJIS,
            Permission.ACCESS_VIP_CHANNELS,
            Permission.DELETE_MESSAGES,
            Permission.MUTE_USERS,
            Permission.VIEW_AUDIT_LOG
        )),

    ADMIN("Admin", 5, "#ef4444", "👑",
        "Community administrator with administrative privileges",
        Arrays.asList(
            Permission.SEND_MESSAGES,
            Permission.SEND_LINKS,
            Permission.UPLOAD_IMAGES,
            Permission.USE_CUSTOM_EMOJIS,
            Permission.ACCESS_VIP_CHANNELS,
            Permission.DELETE_MESSAGES,
            Permission.MUTE_USERS,
            Permission.BAN_USERS,
            Permission.VIEW_AUDIT_LOG,
            Permission.MANAGE_ROLES,
            Permission.ACCESS_ADMIN_PANEL,
            Permission.VIEW_SYSTEM_STATS
        )),

    SUPER_ADMIN("Super Admin", 6, "#dc2626", "🔥",
        "Super administrator with full system control",
        Arrays.asList(
            // All permissions including system-level permissions
            Permission.SEND_MESSAGES,
            Permission.SEND_LINKS,
            Permission.UPLOAD_IMAGES,
            Permission.USE_CUSTOM_EMOJIS,
            Permission.ACCESS_VIP_CHANNELS,
            Permission.DELETE_MESSAGES,
            Permission.MUTE_USERS,
            Permission.BAN_USERS,
            Permission.VIEW_AUDIT_LOG,
            Permission.MANAGE_ROLES,
            Permission.ACCESS_ADMIN_PANEL,
            Permission.VIEW_SYSTEM_STATS,
            Permission.SYSTEM_CONFIGURATION,
            Permission.DATABASE_ACCESS,
            Permission.MANAGE_PERMISSIONS,
            Permission.OWNERSHIP_TRANSFER
        ));

    private final String displayName;
    private final int level;
    private final String color;
    private final String icon;
    private final String description;
    private final List<Permission> permissions;

    UserRole(String displayName, int level, String color, String icon,
             String description, List<Permission> permissions) {
        this.displayName = displayName;
        this.level = level;
        this.color = color;
        this.icon = icon;
        this.description = description;
        this.permissions = permissions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public Set<String> getPermissionNames() {
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    /**
     * Check if this role has a specific permission by name
     */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    /**
     * Check if this role is higher than or equal to the target role
     */
    public boolean isHigherThanOrEqual(UserRole targetRole) {
        return this.level >= targetRole.level;
    }

    /**
     * Check if this role can promote users to the target role
     * Users can only promote to roles lower than their own
     */
    public boolean canPromoteTo(UserRole targetRole) {
        // Super Admin can promote to any role except transferring ownership
        if (this == SUPER_ADMIN && targetRole != SUPER_ADMIN) {
            return true;
        }
        // Admin can promote up to Moderator
        if (this == ADMIN && targetRole.level <= MODERATOR.level) {
            return true;
        }
        // Moderator can promote up to VIP
        if (this == MODERATOR && targetRole.level <= VIP.level) {
            return true;
        }
        return false;
    }

    /**
     * Get the next role in the hierarchy for automatic progression
     */
    public UserRole getNextRole() {
        switch (this) {
            case NEW_MEMBER:
                return MEMBER;
            case MEMBER:
                return VIP;
            case VIP:
                return MODERATOR; // Requires manual approval
            case MODERATOR:
                return ADMIN; // Requires manual assignment
            case ADMIN:
                return SUPER_ADMIN; // Requires manual assignment
            case SUPER_ADMIN:
            default:
                return SUPER_ADMIN; // Highest level
        }
    }

    /**
     * Check if progression to next role requires manual approval
     */
    public boolean requiresManualProgression() {
        return this == VIP || this == MODERATOR || this == ADMIN;
    }

    /**
     * Find role by string name (case insensitive)
     */
    public static UserRole fromString(String roleName) {
        if (roleName == null) {
            return NEW_MEMBER; // Default role
        }

        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(roleName.trim()))
                .findFirst()
                .orElse(NEW_MEMBER);
    }

    /**
     * Get all roles sorted by hierarchy level
     */
    public static List<UserRole> getRolesByHierarchy() {
        return Arrays.stream(values())
                .sorted((r1, r2) -> Integer.compare(r1.level, r2.level))
                .collect(Collectors.toList());
    }

    /**
     * Get progression criteria for this role
     */
    public RoleProgressionCriteria getProgressionCriteria() {
        switch (this) {
            case NEW_MEMBER:
                return new RoleProgressionCriteria(7, 10, 0); // 7 days, 10 messages
            case MEMBER:
                return new RoleProgressionCriteria(30, 100, 0); // 30 days, 100 messages
            case VIP:
                return new RoleProgressionCriteria(0, 0, 1); // Manual approval required
            case MODERATOR:
                return new RoleProgressionCriteria(0, 0, 2); // Admin assignment required
            case ADMIN:
                return new RoleProgressionCriteria(0, 0, 3); // Super Admin assignment required
            case SUPER_ADMIN:
            default:
                return new RoleProgressionCriteria(0, 0, 4); // Ownership transfer required
        }
    }

    /**
     * Inner class to define role progression criteria
     */
    public static class RoleProgressionCriteria {
        private final int minDaysInRole;
        private final int minMessagesSent;
        private final int approvalLevel; // 0=auto, 1=moderator approval, 2=admin approval, etc.

        public RoleProgressionCriteria(int minDaysInRole, int minMessagesSent, int approvalLevel) {
            this.minDaysInRole = minDaysInRole;
            this.minMessagesSent = minMessagesSent;
            this.approvalLevel = approvalLevel;
        }

        public int getMinDaysInRole() {
            return minDaysInRole;
        }

        public int getMinMessagesSent() {
            return minMessagesSent;
        }

        public int getApprovalLevel() {
            return approvalLevel;
        }

        public boolean isAutomatic() {
            return approvalLevel == 0;
        }

        public boolean requiresModeratorApproval() {
            return approvalLevel == 1;
        }

        public boolean requiresAdminApproval() {
            return approvalLevel == 2;
        }

        public boolean requiresSuperAdminApproval() {
            return approvalLevel == 3;
        }

        public boolean requiresOwnershipTransfer() {
            return approvalLevel == 4;
        }
    }
}