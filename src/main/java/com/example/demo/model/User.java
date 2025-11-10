package com.example.demo.model;

import java.time.LocalDateTime;

import com.example.demo.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "users", indexes = {
    @Index(columnList = "email", name = "ux_users_email"),
    @Index(columnList = "user_role", name = "idx_users_role"),
    @Index(columnList = "last_activity_at", name = "idx_users_activity")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Column(nullable = false, length = 255)
    private String email;

    private String gender;
    private Integer age;

    @Column(length = 100)
    private String status;

    @Column(length = 500)
    private String description;

    @Column(length = 1000)
    private String story;

    @Column(columnDefinition = "LONGTEXT")
    private String profilePicture;

    // === NEW ROLE SYSTEM FIELDS ===

    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole = UserRole.NEW_MEMBER;

    @Column(name = "role_assigned_at")
    private LocalDateTime roleAssignedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "role_changed_by", length = 255)
    private String roleChangedBy;

    // Activity tracking for role progression
    @Column(name = "message_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer messageCount = 0;

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt;

    @Column(name = "last_role_progression_check")
    private LocalDateTime lastRoleProgressionCheck;

    // === LEGACY ADMIN FIELDS (kept for backward compatibility) ===

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdmin = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isMuted = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isBanned = false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSuperAdmin = false;

    // === NEW ROLE SYSTEM GETTERS & SETTERS ===

    public UserRole getUserRole() {
        return this.userRole != null ? this.userRole : UserRole.NEW_MEMBER;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
        // Auto-set role assignment timestamp if not set
        if (this.roleAssignedAt == null) {
            this.roleAssignedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getRoleAssignedAt() {
        return this.roleAssignedAt;
    }

    public void setRoleAssignedAt(LocalDateTime roleAssignedAt) {
        this.roleAssignedAt = roleAssignedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return this.lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getRoleChangedBy() {
        return this.roleChangedBy;
    }

    public void setRoleChangedBy(String roleChangedBy) {
        this.roleChangedBy = roleChangedBy;
    }

    public Integer getMessageCount() {
        return this.messageCount != null ? this.messageCount : 0;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public void incrementMessageCount() {
        this.messageCount = (this.messageCount != null ? this.messageCount : 0) + 1;
    }

    public LocalDateTime getAccountCreatedAt() {
        return this.accountCreatedAt;
    }

    public void setAccountCreatedAt(LocalDateTime accountCreatedAt) {
        this.accountCreatedAt = accountCreatedAt;
    }

    public LocalDateTime getLastRoleProgressionCheck() {
        return this.lastRoleProgressionCheck;
    }

    public void setLastRoleProgressionCheck(LocalDateTime lastRoleProgressionCheck) {
        this.lastRoleProgressionCheck = lastRoleProgressionCheck;
    }

    // === LEGACY FIELD GETTERS & SETTERS (for backward compatibility) ===

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getGender() { return this.gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() { return this.age; }
    public void setAge(Integer age) { this.age = age; }

    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public String getStory() { return this.story; }
    public void setStory(String story) { this.story = story; }

    public String getProfilePicture() { return this.profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public Boolean getIsAdmin() { return this.isAdmin != null ? this.isAdmin : false; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public Boolean getIsMuted() { return this.isMuted != null ? this.isMuted : false; }
    public void setIsMuted(Boolean isMuted) { this.isMuted = isMuted; }

    public Boolean getIsBanned() { return this.isBanned != null ? this.isBanned : false; }
    public void setIsBanned(Boolean isBanned) { this.isBanned = isBanned; }

    public Boolean getIsSuperAdmin() { return this.isSuperAdmin != null ? this.isSuperAdmin : false; }
    public void setIsSuperAdmin(Boolean isSuperAdmin) { this.isSuperAdmin = isSuperAdmin; }

    // === UTILITY METHODS ===

    /**
     * Check if user has a specific permission based on their role
     */
    public boolean hasPermission(com.example.demo.permissions.Permission permission) {
        return getUserRole().hasPermission(permission);
    }

    /**
     * Check if user has a specific permission by name
     */
    public boolean hasPermission(String permissionName) {
        return getUserRole().hasPermission(permissionName);
    }

    /**
     * Check if user can perform moderation actions
     */
    public boolean canModerate() {
        return getUserRole().getLevel() >= UserRole.MODERATOR.getLevel();
    }

    /**
     * Check if user can perform administrative actions
     */
    public boolean canAdministrate() {
        return getUserRole().getLevel() >= UserRole.ADMIN.getLevel();
    }

    /**
     * Check if user is online based on last activity
     */
    public boolean isOnline() {
        if (lastActivityAt == null) {
            return false;
        }
        // Consider user online if activity was within last 5 minutes
        return lastActivityAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * Update last activity timestamp
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Get account age in days
     */
    public long getAccountAgeInDays() {
        if (accountCreatedAt == null) {
            return 0;
        }
        return java.time.Duration.between(accountCreatedAt, LocalDateTime.now()).toDays();
    }

    /**
     * Get days since role was assigned
     */
    public long getDaysInCurrentRole() {
        if (roleAssignedAt == null) {
            return 0;
        }
        return java.time.Duration.between(roleAssignedAt, LocalDateTime.now()).toDays();
    }

    /**
     * Check if user is eligible for automatic role progression
     */
    public boolean isEligibleForProgression() {
        UserRole nextRole = getUserRole().getNextRole();
        if (nextRole == getUserRole()) {
            return false; // Already at highest level
        }

        UserRole.RoleProgressionCriteria criteria = getUserRole().getProgressionCriteria();
        if (!criteria.isAutomatic()) {
            return false; // Requires manual approval
        }

        return getDaysInCurrentRole() >= criteria.getMinDaysInRole() &&
               getMessageCount() >= criteria.getMinMessagesSent();
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', email='%s', role=%s, messages=%d}",
                           id, username, email, userRole, messageCount);
    }
}
