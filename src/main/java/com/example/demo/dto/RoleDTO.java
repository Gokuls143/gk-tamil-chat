package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.demo.enums.UserRole;
import com.example.demo.permissions.Permission;

/**
 * Data Transfer Object for role information
 * Used for API responses and frontend consumption
 */
public class RoleDTO {
    private String name;
    private String displayName;
    private int level;
    private String color;
    private String icon;
    private String description;
    private List<String> permissions;
    private Map<String, Object> progressionCriteria;
    private boolean requiresManualProgression;

    // Constructor for full role information
    public RoleDTO(UserRole role) {
        this.name = role.name();
        this.displayName = role.getDisplayName();
        this.level = role.getLevel();
        this.color = role.getColor();
        this.icon = role.getIcon();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream()
                .map(Permission::getName)
                .toList();
        this.requiresManualProgression = role.requiresManualProgression();

        // Add progression criteria
        UserRole.RoleProgressionCriteria criteria = role.getProgressionCriteria();
        this.progressionCriteria = Map.of(
            "minDaysInRole", criteria.getMinDaysInRole(),
            "minMessagesSent", criteria.getMinMessagesSent(),
            "isAutomatic", criteria.isAutomatic(),
            "approvalLevel", criteria.getApprovalLevel()
        );
    }

    // Constructor for minimal role information
    public RoleDTO(String name, String displayName, int level, String color, String icon) {
        this.name = name;
        this.displayName = displayName;
        this.level = level;
        this.color = color;
        this.icon = icon;
    }

    // === USER ROLE INFORMATION ===

    public static class UserRoleInfo {
        private String username;
        private String currentRole;
        private String currentRoleDisplayName;
        private int roleLevel;
        private List<String> permissions;
        private LocalDateTime roleAssignedAt;
        private String roleChangedBy;
        private int messageCount;
        private long accountAgeDays;
        private long daysInCurrentRole;
        private boolean eligibleForProgression;
        private NextRoleInfo nextRole;

        // Constructor
        public UserRoleInfo(String username, UserRole role, LocalDateTime roleAssignedAt,
                           String roleChangedBy, int messageCount, long accountAgeDays,
                           long daysInCurrentRole, boolean eligibleForProgression) {
            this.username = username;
            this.currentRole = role.name();
            this.currentRoleDisplayName = role.getDisplayName();
            this.roleLevel = role.getLevel();
            this.permissions = role.getPermissions().stream()
                    .map(Permission::getName)
                    .toList();
            this.roleAssignedAt = roleAssignedAt;
            this.roleChangedBy = roleChangedBy;
            this.messageCount = messageCount;
            this.accountAgeDays = accountAgeDays;
            this.daysInCurrentRole = daysInCurrentRole;
            this.eligibleForProgression = eligibleForProgression;

            // Add next role info if not at highest level
            UserRole nextRole = role.getNextRole();
            if (nextRole != role) {
                this.nextRole = new NextRoleInfo(nextRole);
            }
        }

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getCurrentRole() { return currentRole; }
        public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }

        public String getCurrentRoleDisplayName() { return currentRoleDisplayName; }
        public void setCurrentRoleDisplayName(String currentRoleDisplayName) { this.currentRoleDisplayName = currentRoleDisplayName; }

        public int getRoleLevel() { return roleLevel; }
        public void setRoleLevel(int roleLevel) { this.roleLevel = roleLevel; }

        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }

        public LocalDateTime getRoleAssignedAt() { return roleAssignedAt; }
        public void setRoleAssignedAt(LocalDateTime roleAssignedAt) { this.roleAssignedAt = roleAssignedAt; }

        public String getRoleChangedBy() { return roleChangedBy; }
        public void setRoleChangedBy(String roleChangedBy) { this.roleChangedBy = roleChangedBy; }

        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

        public long getAccountAgeDays() { return accountAgeDays; }
        public void setAccountAgeDays(long accountAgeDays) { this.accountAgeDays = accountAgeDays; }

        public long getDaysInCurrentRole() { return daysInCurrentRole; }
        public void setDaysInCurrentRole(long daysInCurrentRole) { this.daysInCurrentRole = daysInCurrentRole; }

        public boolean isEligibleForProgression() { return eligibleForProgression; }
        public void setEligibleForProgression(boolean eligibleForProgression) { this.eligibleForProgression = eligibleForProgression; }

        public NextRoleInfo getNextRole() { return nextRole; }
        public void setNextRole(NextRoleInfo nextRole) { this.nextRole = nextRole; }
    }

    /**
     * Information about the next role in progression
     */
    public static class NextRoleInfo {
        private String name;
        private String displayName;
        private String color;
        private String icon;
        private boolean requiresManualApproval;

        public NextRoleInfo(UserRole role) {
            this.name = role.name();
            this.displayName = role.getDisplayName();
            this.color = role.getColor();
            this.icon = role.getIcon();
            this.requiresManualApproval = role.requiresManualProgression();
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public boolean isRequiresManualApproval() { return requiresManualApproval; }
        public void setRequiresManualApproval(boolean requiresManualApproval) { this.requiresManualApproval = requiresManualApproval; }
    }

    /**
     * User list information with role details
     */
    public static class UserListInfo {
        private String username;
        private String email;
        private String role;
        private String roleDisplayName;
        private int messageCount;
        private long accountAgeDays;
        private long daysInCurrentRole;
        private LocalDateTime roleAssignedAt;
        private String roleChangedBy;
        private LocalDateTime lastActivityAt;
        private boolean isOnline;
        private boolean eligibleForProgression;

        // Constructor
        public UserListInfo(String username, String email, UserRole role, int messageCount,
                           long accountAgeDays, long daysInCurrentRole, LocalDateTime roleAssignedAt,
                           String roleChangedBy, LocalDateTime lastActivityAt, boolean isOnline,
                           boolean eligibleForProgression) {
            this.username = username;
            this.email = email;
            this.role = role.name();
            this.roleDisplayName = role.getDisplayName();
            this.messageCount = messageCount;
            this.accountAgeDays = accountAgeDays;
            this.daysInCurrentRole = daysInCurrentRole;
            this.roleAssignedAt = roleAssignedAt;
            this.roleChangedBy = roleChangedBy;
            this.lastActivityAt = lastActivityAt;
            this.isOnline = isOnline;
            this.eligibleForProgression = eligibleForProgression;
        }

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getRoleDisplayName() { return roleDisplayName; }
        public void setRoleDisplayName(String roleDisplayName) { this.roleDisplayName = roleDisplayName; }

        public int getMessageCount() { return messageCount; }
        public void setMessageCount(int messageCount) { this.messageCount = messageCount; }

        public long getAccountAgeDays() { return accountAgeDays; }
        public void setAccountAgeDays(long accountAgeDays) { this.accountAgeDays = accountAgeDays; }

        public long getDaysInCurrentRole() { return daysInCurrentRole; }
        public void setDaysInCurrentRole(long daysInCurrentRole) { this.daysInCurrentRole = daysInCurrentRole; }

        public LocalDateTime getRoleAssignedAt() { return roleAssignedAt; }
        public void setRoleAssignedAt(LocalDateTime roleAssignedAt) { this.roleAssignedAt = roleAssignedAt; }

        public String getRoleChangedBy() { return roleChangedBy; }
        public void setRoleChangedBy(String roleChangedBy) { this.roleChangedBy = roleChangedBy; }

        public LocalDateTime getLastActivityAt() { return lastActivityAt; }
        public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

        public boolean isOnline() { return isOnline; }
        public void setOnline(boolean online) { isOnline = online; }

        public boolean isEligibleForProgression() { return eligibleForProgression; }
        public void setEligibleForProgression(boolean eligibleForProgression) { this.eligibleForProgression = eligibleForProgression; }
    }

    /**
     * Role assignment/demotion operation result
     */
    public static class RoleOperationResult {
        private boolean success;
        private String message;
        private String username;
        private String previousRole;
        private String newRole;
        private String operationType;
        private String performedBy;
        private LocalDateTime performedAt;

        public RoleOperationResult(boolean success, String message, String username,
                                  String previousRole, String newRole, String operationType,
                                  String performedBy, LocalDateTime performedAt) {
            this.success = success;
            this.message = message;
            this.username = username;
            this.previousRole = previousRole;
            this.newRole = newRole;
            this.operationType = operationType;
            this.performedBy = performedBy;
            this.performedAt = performedAt;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPreviousRole() { return previousRole; }
        public void setPreviousRole(String previousRole) { this.previousRole = previousRole; }

        public String getNewRole() { return newRole; }
        public void setNewRole(String newRole) { this.newRole = newRole; }

        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }

        public String getPerformedBy() { return performedBy; }
        public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

        public LocalDateTime getPerformedAt() { return performedAt; }
        public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
    }

    // === GETTERS AND SETTERS FOR MAIN RoleDTO ===

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public Map<String, Object> getProgressionCriteria() { return progressionCriteria; }
    public void setProgressionCriteria(Map<String, Object> progressionCriteria) { this.progressionCriteria = progressionCriteria; }

    public boolean isRequiresManualProgression() { return requiresManualProgression; }
    public void setRequiresManualProgression(boolean requiresManualProgression) { this.requiresManualProgression = requiresManualProgression; }

    @Override
    public String toString() {
        return String.format("RoleDTO{name='%s', displayName='%s', level=%d, color='%s', icon='%s'}",
                           name, displayName, level, color, icon);
    }
}